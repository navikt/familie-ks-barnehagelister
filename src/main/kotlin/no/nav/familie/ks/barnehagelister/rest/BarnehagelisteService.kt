package no.nav.familie.ks.barnehagelister.rest

import no.nav.familie.ks.barnehagelister.domene.Barnehagelister
import no.nav.familie.ks.barnehagelister.kontrakt.SkjemaV1
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisterRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class BarnehagelisteService(
    private val barnehagelisterRepository: BarnehagelisterRepository,
) {
    private val logger = LoggerFactory.getLogger(BarnehagelisteService::class.java)

    fun mottaBarnehagelister(skjemaV1: SkjemaV1): ResponseEntity<BarnehagelisteResponse> {
        logger.info("Mottok skjema")
        // val skjemaV1: SkjemaV1 = objectMapper.readValue(skjemaV1String, SkjemaV1::class.java)

        val barnehageliste = barnehagelisterRepository.findByIdOrNull(skjemaV1.id)
        return if (barnehageliste == null) {
            val innsendtListe = barnehagelisterRepository.insert(Barnehagelister(skjemaV1.id, skjemaV1, "MOTTATT"))
            ResponseEntity.accepted().body(
                BarnehagelisteResponse(
                    id = skjemaV1.id,
                    status = "MOTTATT",
                    mottattTid = innsendtListe.opprettetTid,
                    ferdigTid = innsendtListe.ferdigTid,
                    links =
                        ResponseLinker(
                            status = "/api/barnehagelister/status/${skjemaV1.id}",
                        ),
                ),
            )
        } else {
            val httpStatusKode = if (barnehageliste.erFerdigProsessert()) HttpStatus.OK else HttpStatus.ACCEPTED
            ResponseEntity.status(httpStatusKode).body(
                BarnehagelisteResponse(
                    id = skjemaV1.id,
                    status = barnehageliste.status,
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
        logger.info("Mottok status")
        val barnehageliste = barnehagelisterRepository.findByIdOrNull(transaksjonsId)
        return if (barnehageliste == null) {
            ResponseEntity.notFound().build()
        } else {
            val httpStatusKode = if (barnehageliste.erFerdigProsessert()) HttpStatus.OK else HttpStatus.ACCEPTED
            ResponseEntity.status(httpStatusKode).body(
                BarnehagelisteResponse(
                    id = transaksjonsId,
                    status = barnehageliste.status,
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
