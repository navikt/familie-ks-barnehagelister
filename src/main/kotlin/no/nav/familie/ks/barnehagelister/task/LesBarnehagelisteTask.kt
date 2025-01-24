package no.nav.familie.ks.barnehagelister.task

import no.nav.familie.ks.barnehagelister.domene.mapTilBarnehagebarn
import no.nav.familie.ks.barnehagelister.repository.BarnehagebarnRepository
import no.nav.familie.ks.barnehagelister.rest.dto.BarnehagelisteStatus
import no.nav.familie.ks.barnehagelister.service.BarnehagelisteService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.Properties
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = LesBarnehagelisteTask.TASK_STEP_TYPE,
    beskrivelse = "Har mottatt barnehagelister. Validerer og trigger videre håndtering.",
    maxAntallFeil = 3,
    triggerTidVedFeilISekunder = 5,
)
class LesBarnehagelisteTask(
    private val barnehagebarnRepository: BarnehagebarnRepository,
    private val taskService: TaskService,
    private val barnehagelisteService: BarnehagelisteService,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        logger.info("Kjører task for mottak av ny barnehageliste.")
        val barnehagelisteId = UUID.fromString(task.payload)

        val barnehageliste =
            barnehagelisteService.hentBarnehageliste(barnehagelisteId) ?: error("Fant ikke barnehageliste med id $barnehagelisteId")

        if (barnehageliste.status == BarnehagelisteStatus.FERDIG) {
            return
        }

        val barnehagebarn = barnehageliste.rawJson.mapTilBarnehagebarn()

        barnehagebarnRepository.insertAll(barnehagebarn)

        barnehagebarn.forEach { barn ->
            taskService.save(SendBarnehagebarnTilKsTask.opprettTask(barn.id))
        }

        barnehagelisteService.settBarnehagelisteStatusTilFerdig(barnehageliste)
    }

    companion object {
        const val TASK_STEP_TYPE = "lesBarnehagelisteTask"
        private val logger: Logger = LoggerFactory.getLogger(LesBarnehagelisteTask::class.java)

        fun opprettTask(barnehagelisteId: UUID): Task =
            Task(
                type = TASK_STEP_TYPE,
                payload = barnehagelisteId.toString(),
                properties =
                    Properties().apply {
                        this["barnehagelisteId"] = barnehagelisteId.toString()
                    },
            )
    }
}
