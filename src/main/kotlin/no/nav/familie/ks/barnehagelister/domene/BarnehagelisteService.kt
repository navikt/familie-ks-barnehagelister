package no.nav.familie.ks.barnehagelister.domene

import no.nav.familie.ks.barnehagelister.repository.BarnehagelisterRepository
import no.nav.familie.ks.barnehagelister.rest.dto.BarnehagelisteStatus
import no.nav.familie.ks.barnehagelister.task.MottattBarnehagelisteTask
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class BarnehagelisteService(
    private val barnehagelisterRepository: BarnehagelisterRepository,
    val taskService: TaskService,
) {
    private val logger = LoggerFactory.getLogger(BarnehagelisteService::class.java)

    fun mottaBarnehagelister(skjemaV1: SkjemaV1): Barnehagelister {
        val eksisterendeBarnehageliste = barnehagelisterRepository.findByIdOrNull(skjemaV1.id)
        if (eksisterendeBarnehageliste != null) return eksisterendeBarnehageliste

        val lagretBarnehageliste =
            barnehagelisterRepository
                .insert(
                    Barnehagelister(
                        skjemaV1.id,
                        skjemaV1,
                        BarnehagelisteStatus.MOTTATT,
                    ),
                ).also {
                    MottattBarnehagelisteTask.opprettTask(skjemaV1.id).also {
                        taskService.save(it)
                    }
                }

        return lagretBarnehageliste
    }

    fun hentBarnehagelister(barnehagelisterId: UUID): Barnehagelister? {
        logger.info("Henter barnehagelister m/ id $barnehagelisterId")

        return barnehagelisterRepository.findByIdOrNull(barnehagelisterId)
    }
}
