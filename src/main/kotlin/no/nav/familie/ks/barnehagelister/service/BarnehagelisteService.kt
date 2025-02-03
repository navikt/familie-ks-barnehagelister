package no.nav.familie.ks.barnehagelister.service

import no.nav.familie.ks.barnehagelister.domene.Barnehageliste
import no.nav.familie.ks.barnehagelister.domene.BarnehagelisteValideringsfeil
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisteRepository
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisteValideringsfeilRepository
import no.nav.familie.ks.barnehagelister.rest.dto.BarnehagelisteStatus
import no.nav.familie.ks.barnehagelister.rest.dto.FormV1RequestDto
import no.nav.familie.ks.barnehagelister.task.PeriodeOverlappValideringTask
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class BarnehagelisteService(
    private val barnehagelisteRepository: BarnehagelisteRepository,
    private val taskService: TaskService,
    private val barnehagelisteValideringsfeilRepository: BarnehagelisteValideringsfeilRepository,
) {
    private val logger = LoggerFactory.getLogger(BarnehagelisteService::class.java)

    fun mottaBarnehageliste(
        skjemaV1: FormV1RequestDto,
        leverandørOrgNr: String,
        kommuneOrgNr: String,
    ): BarnehagelisteMedValideringsfeil {
        val eksisterendeBarnehagelisteMedValideringsfeil = hentBarnehagelisteMedValideringsfeil(skjemaV1.id)
        val eksisterendeBarnehageliste = eksisterendeBarnehagelisteMedValideringsfeil.barnehageliste
        if (eksisterendeBarnehageliste != null) {
            logger.info("Barnehagelister med id ${skjemaV1.id} har allerede blitt mottatt tidligere.")
            return BarnehagelisteMedValideringsfeil(
                barnehageliste = eksisterendeBarnehageliste,
                valideringsfeil =
                    if (eksisterendeBarnehageliste.status == BarnehagelisteStatus.FERDIG) {
                        eksisterendeBarnehagelisteMedValideringsfeil.valideringsfeil
                    } else {
                        emptyList()
                    },
            )
        }

        val lagretBarnehageliste =
            barnehagelisteRepository
                .insert(
                    Barnehageliste(
                        id = skjemaV1.id,
                        rawJson = skjemaV1,
                        status = BarnehagelisteStatus.MOTTATT,
                        leverandorOrgNr = leverandørOrgNr,
                        kommuneOrgNr = kommuneOrgNr,
                    ),
                )

        val opprettetTask =
            PeriodeOverlappValideringTask.opprettTask(
                barnehagelisteId = skjemaV1.id.toString(),
                leverandørOrgNr = leverandørOrgNr,
                kommuneOrgNr = kommuneOrgNr,
            )
        taskService.save(opprettetTask)

        return BarnehagelisteMedValideringsfeil(
            barnehageliste = lagretBarnehageliste,
            valideringsfeil = emptyList(),
        )
    }

    fun hentBarnehageliste(barnehagelisteId: UUID): Barnehageliste? {
        logger.info("Henter barnehagelister m/ id $barnehagelisteId")

        return barnehagelisteRepository.findByIdOrNull(barnehagelisteId)
    }

    fun hentBarnehagelisteMedValideringsfeil(barnehagelisterId: UUID): BarnehagelisteMedValideringsfeil {
        val barnehageliste = hentBarnehageliste(barnehagelisterId)
        val valideringsfeil = barnehagelisteValideringsfeilRepository.findByBarnehagelisteId(barnehagelisterId)
        return BarnehagelisteMedValideringsfeil(barnehageliste, valideringsfeil)
    }

    fun settBarnehagelisteStatusTilFerdig(barnehageliste: Barnehageliste) {
        barnehagelisteRepository.update(
            barnehageliste
                .copy(
                    status = BarnehagelisteStatus.FERDIG,
                    ferdigTid = LocalDateTime.now(),
                ),
        )
    }
}

data class BarnehagelisteMedValideringsfeil(
    val barnehageliste: Barnehageliste?,
    val valideringsfeil: List<BarnehagelisteValideringsfeil>,
)
