package no.nav.familie.ks.barnehagelister.task

import no.nav.familie.ks.barnehagelister.repository.BarnehagelisterRepository
import no.nav.familie.ks.barnehagelister.rest.dto.BarnehagelisteStatus
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.Properties
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = MottattBarnehagelisteTask.TASK_STEP_TYPE,
    beskrivelse = "Har mottatt barnehagelister. Validerer og trigger videre håndtering.",
    maxAntallFeil = 3,
    triggerTidVedFeilISekunder = 5,
)
class MottattBarnehagelisteTask(
    private val barnehagelisterRepository: BarnehagelisterRepository,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        logger.info("Kjører task for mottak av ny barnehageliste.")
        val barnehagelisteId = UUID.fromString(task.payload)

        val barnehageliste =
            barnehagelisterRepository.findByIdOrNull(barnehagelisteId) ?: error("Fant ikke barnehageliste med id $barnehagelisteId")

        barnehageliste
            .copy(
                status = BarnehagelisteStatus.FERDIG,
                ferdigTid = LocalDateTime.now(),
            ).also {
                barnehagelisterRepository.update(it)
            }
    }

    companion object {
        const val TASK_STEP_TYPE = "mottattBarnehagelisteTask"
        private val logger: Logger = LoggerFactory.getLogger(MottattBarnehagelisteTask::class.java)

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
