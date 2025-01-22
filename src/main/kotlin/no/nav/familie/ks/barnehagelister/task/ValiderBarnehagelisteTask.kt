package no.nav.familie.ks.barnehagelister.task

import no.nav.familie.ks.barnehagelister.domene.mapTilBarnehagebarn
import no.nav.familie.ks.barnehagelister.repository.BarnehagebarnRepository
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisteRepository
import no.nav.familie.ks.barnehagelister.validering.validerIngenOverlapp
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.Properties
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = ValiderBarnehagelisteTask.TASK_STEP_TYPE,
    beskrivelse = "Valider barnehageliste og send til videre håndtering.",
    maxAntallFeil = 3,
    triggerTidVedFeilISekunder = 5,
)
class ValiderBarnehagelisteTask(
    private val barnehagebarnRepository: BarnehagebarnRepository,
    private val barnehagelisteRepository: BarnehagelisteRepository,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        val barnehagelisteId = UUID.fromString(task.payload)
        val barnehageliste =
            barnehagelisteRepository.findByIdOrNull(barnehagelisteId) ?: error("Fant ikke barnehageliste med id $barnehagelisteId")

        val json = barnehageliste.rawJson

        val listaGruppertPåBarn = json.mapTilBarnehagebarn().groupBy { barn -> barn.ident }

        listaGruppertPåBarn.forEach { barn, listeMedBarnehagebarn ->
            listeMedBarnehagebarn.validerIngenOverlapp()
        }
    }

    override fun onCompletion(task: Task) {
        LesBarnehagelisteTask.opprettTask(UUID.fromString(task.payload))
    }

    companion object {
        const val TASK_STEP_TYPE = "validerBarnehagelisteTask"
        private val logger: Logger = LoggerFactory.getLogger(ValiderBarnehagelisteTask::class.java)

        fun opprettTask(barnehagelisteId: String): Task =
            Task(
                type = TASK_STEP_TYPE,
                payload = barnehagelisteId,
                properties =
                    Properties().apply {
                        this["barnehagelisteId"] = barnehagelisteId
                    },
            )
    }
}
