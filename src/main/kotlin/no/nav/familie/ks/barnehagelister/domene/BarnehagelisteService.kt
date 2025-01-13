package no.nav.familie.ks.barnehagelister.domene

import no.nav.familie.ks.barnehagelister.repository.BarnehagelisterRepository
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
    private val barnehagelisterRepository: BarnehagelisterRepository,
    private val taskService: TaskService,
) {
    private val logger = LoggerFactory.getLogger(BarnehagelisteService::class.java)

    fun mottaBarnehagelister(
        skjemaV1: SkjemaV1,
        leverandørOrgNr: String,
    ): Barnehagelister {
        val eksisterendeBarnehageliste = barnehagelisterRepository.findByIdOrNull(skjemaV1.id)
        if (eksisterendeBarnehageliste != null) {
            logger.info("Barnehagelister med id ${skjemaV1.id} har allerede blitt mottatt tidligere.")
            return eksisterendeBarnehageliste
        }

        val lagretBarnehageliste =
            barnehagelisterRepository
                .insert(
                    Barnehagelister(
                        id = skjemaV1.id,
                        rawJson = skjemaV1,
                        status = BarnehagelisteStatus.MOTTATT,
                        leverandorOrgNr = leverandørOrgNr,
                    ),
                )

        val opprettetTask = LesBarnehagelisteTask.opprettTask(skjemaV1.id)
        taskService.save(opprettetTask)

        return lagretBarnehageliste
    }

    fun hentBarnehagelister(barnehagelisterId: UUID): Barnehagelister? {
        logger.info("Henter barnehagelister m/ id $barnehagelisterId")

        return barnehagelisterRepository.findByIdOrNull(barnehagelisterId)
    }

    fun setListeFerdig(barnehageliste: Barnehagelister){
        barnehagelisterRepository.update(barnehageliste
            .copy(
                status = BarnehagelisteStatus.FERDIG,
                ferdigTid = LocalDateTime.now(),
            )
        )
    }
}
