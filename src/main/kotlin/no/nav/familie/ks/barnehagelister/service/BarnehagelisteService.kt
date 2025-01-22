package no.nav.familie.ks.barnehagelister.service

import no.nav.familie.ks.barnehagelister.domene.Barnehageliste
import no.nav.familie.ks.barnehagelister.domene.SkjemaV1
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisteRepository
import no.nav.familie.ks.barnehagelister.rest.dto.BarnehagelisteStatus
import no.nav.familie.ks.barnehagelister.task.LesBarnehagelisteTask
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
) {
    private val logger = LoggerFactory.getLogger(BarnehagelisteService::class.java)

    fun mottaBarnehageliste(
        skjemaV1: SkjemaV1,
        leverandørOrgNr: String,
        kommuneOrgNr: String,
    ): Barnehageliste {
        val eksisterendeBarnehageliste = barnehagelisteRepository.findByIdOrNull(skjemaV1.id)
        if (eksisterendeBarnehageliste != null) {
            logger.info("Barnehagelister med id ${skjemaV1.id} har allerede blitt mottatt tidligere.")
            return eksisterendeBarnehageliste
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

        val opprettetTask = LesBarnehagelisteTask.opprettTask(skjemaV1.id)
        taskService.save(opprettetTask)

        return lagretBarnehageliste
    }

    fun hentBarnehageliste(barnehagelisterId: UUID): Barnehageliste? {
        logger.info("Henter barnehagelister m/ id $barnehagelisterId")

        return barnehagelisteRepository.findByIdOrNull(barnehagelisterId)
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
