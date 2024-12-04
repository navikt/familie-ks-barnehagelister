package no.nav.familie.ks.barnehagelister.rest

import no.nav.familie.ks.barnehagelister.domene.Barnehagelister
import no.nav.familie.ks.barnehagelister.kontrakt.SkjemaV1
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisterRepository
import no.nav.familie.ks.barnehagelister.task.MottattBarnehagelisteTask
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import java.util.UUID

@Service
class BarnehagelisteService(
    private val barnehagelisterRepository: BarnehagelisterRepository,
    val taskService: TaskService,
) {
    private val logger = LoggerFactory.getLogger(BarnehagelisteService::class.java)

    fun mottaBarnehagelister(
        skjemaV1: SkjemaV1,
        bindingResult: BindingResult,
    ): ResponseEntity<BarnehagelisteResponse> {
        if (bindingResult.hasErrors()) {
            val feil =
                bindingResult.allErrors.map {
                    if (it is FieldError) {
                        ValideringsfeilInfo(it.field, it.defaultMessage ?: "mangler")
                    } else {
                        ValideringsfeilInfo("mangler", it.defaultMessage ?: "mangler")
                    }
                }
            throw ValideringsfeilException(feil)
        }

        val barnehageliste = barnehagelisterRepository.findByIdOrNull(skjemaV1.id)
        return if (barnehageliste == null) {
            val innsendtListe = barnehagelisterRepository.insert(Barnehagelister(skjemaV1.id, skjemaV1, BarnehagelisteStatus.MOTTATT))
            ResponseEntity
                .accepted()
                .body(
                    BarnehagelisteResponse(
                        id = skjemaV1.id,
                        status = BarnehagelisteStatus.MOTTATT.engelsk,
                        mottattTid = innsendtListe.opprettetTid,
                        ferdigTid = innsendtListe.ferdigTid,
                        links =
                            ResponseLinker(
                                status = "/api/barnehagelister/status/${skjemaV1.id}",
                            ),
                    ),
                ).also {
                    MottattBarnehagelisteTask.opprettTask(skjemaV1.id).also {
                        taskService.save(it)
                    }
                }
        } else {
            val httpStatusKode = if (barnehageliste.erFerdigProsessert()) HttpStatus.OK else HttpStatus.ACCEPTED
            ResponseEntity.status(httpStatusKode).body(
                BarnehagelisteResponse(
                    id = skjemaV1.id,
                    status = barnehageliste.status.engelsk,
                    mottattTid = barnehageliste.opprettetTid,
                    ferdigTid = barnehageliste.ferdigTid,
                    links =
                        ResponseLinker(
                            status = "/api/barnehagelister/status/${skjemaV1.id}",
                        ),
                ),
            )
        }
    }

    fun status(transaksjonsId: UUID): ResponseEntity<BarnehagelisteResponse> {
        logger.info("Mottok status for transaksjonsId=$transaksjonsId")
        val barnehageliste = barnehagelisterRepository.findByIdOrNull(transaksjonsId)
        return if (barnehageliste == null) {
            ResponseEntity.notFound().build()
        } else {
            val httpStatusKode = if (barnehageliste.erFerdigProsessert()) HttpStatus.OK else HttpStatus.ACCEPTED
            ResponseEntity.status(httpStatusKode).body(
                BarnehagelisteResponse(
                    id = transaksjonsId,
                    status = barnehageliste.status.engelsk,
                    mottattTid = barnehageliste.opprettetTid,
                    ferdigTid = barnehageliste.ferdigTid,
                    links =
                        ResponseLinker(
                            status = "/api/barnehagelister/status/$transaksjonsId",
                        ),
                ),
            )
        }
    }

    fun ping(): String {
        logger.info("Mottok ping")
        return "\"OK\""
    }
}
