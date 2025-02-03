package no.nav.familie.ks.barnehagelister.task

import no.nav.familie.ks.barnehagelister.domene.BarnehagelisteValideringsfeil
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisteRepository
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisteValideringsfeilRepository
import no.nav.familie.ks.barnehagelister.rest.dto.EtterprosesseringfeilType
import no.nav.familie.ks.barnehagelister.rest.dto.mapTilBarnehagebarn
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
    taskStepType = PeriodeOverlappValideringTask.TASK_STEP_TYPE,
    beskrivelse = "Valider barnehageliste og send til videre håndtering.",
    maxAntallFeil = 3,
    triggerTidVedFeilISekunder = 5,
)
class PeriodeOverlappValideringTask(
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
                    logger.info("Overlappende perioder på barnehageliste $barnehagelisteId")

                    BarnehagelisteValideringsfeil(
                        id = UUID.randomUUID(),
                        barnehagelisteId = barnehagelisteId,
                        etterprosesseringfeiltype = EtterprosesseringfeilType.OVERLAPPING_PERIOD_WITHIN_SAME_LIST,
                        feilinfo = "Overlapping period within the same list for children.",
                        ident = barn,
                    )
                }
            }
        if (alleValideringsfeil.isNotEmpty()) {
            barnehagelisteValideringsfeilRepository.insertAll(alleValideringsfeil)
        }
    }

    override fun onCompletion(task: Task) {
        val barnehagelisteId = UUID.fromString(task.payload)
        val barnehageliste = barnehagelisteRepository.finnById(barnehagelisteId)
        LesBarnehagelisteTask.opprettTask(UUID.fromString(task.payload), barnehageliste.leverandorOrgNr, barnehageliste.kommuneOrgNr).also {
            taskService.save(it)
        }
    }

    companion object {
        const val TASK_STEP_TYPE = "periodeOverlappValideringTask"

        fun opprettTask(
            barnehagelisteId: String,
            leverandørOrgNr: String,
            kommuneOrgNr: String,
        ): Task =
            Task(
                type = TASK_STEP_TYPE,
                payload = barnehagelisteId,
                properties =
                    Properties().apply {
                        this["barnehagelisteId"] = barnehagelisteId
                        this["kommuneOrgNr"] = kommuneOrgNr
                        this["leverandørOrgNr"] = leverandørOrgNr
                    },
            )
    }
}
