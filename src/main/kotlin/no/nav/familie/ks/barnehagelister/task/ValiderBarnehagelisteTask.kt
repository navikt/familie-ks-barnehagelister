package no.nav.familie.ks.barnehagelister.task

import no.nav.familie.ks.barnehagelister.domene.BarnehagelisteValideringsfeil
import no.nav.familie.ks.barnehagelister.domene.mapTilBarnehagebarn
import no.nav.familie.ks.barnehagelister.repository.BarnehagebarnRepository
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisteRepository
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisteValideringsfeilRepository
import no.nav.familie.ks.barnehagelister.validering.validerIngenOverlapp
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
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
    private val barnehagelisteValideringsfeilRepository: BarnehagelisteValideringsfeilRepository,
    private val taskService: TaskService,
) : AsyncTaskStep {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val barnehagelisteId = UUID.fromString(task.payload)
        val barnehageliste =
            barnehagelisteRepository.findByIdOrNull(barnehagelisteId) ?: error("Fant ikke barnehageliste med id $barnehagelisteId")

        val json = barnehageliste.rawJson

        val listaGruppertPåBarn = json.mapTilBarnehagebarn().groupBy { barn -> barn.ident }

        val alleValideringsfeil =
            listaGruppertPåBarn.mapNotNull { (barn, listeMedBarnehagebarn) ->
                try {
                    listeMedBarnehagebarn.validerIngenOverlapp()
                    null
                } catch (e: Exception) {
                    BarnehagelisteValideringsfeil(
                        id = UUID.randomUUID(),
                        barnehagelisteId = barnehagelisteId,
                        type = "OVERLAPPING_PERIOD_WITHIN_SAME_LIST",
                        feilinfo = "Overlapping period within the same list for children.",
                        ident = barn,
                    )
                }
            }

        barnehagelisteValideringsfeilRepository.insertAll(alleValideringsfeil)
    }

    override fun onCompletion(task: Task) {
        LesBarnehagelisteTask.opprettTask(UUID.fromString(task.payload)).also { taskService.save(it) }
    }

    companion object {
        const val TASK_STEP_TYPE = "validerBarnehagelisteTask"

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
