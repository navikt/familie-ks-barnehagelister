package no.nav.familie.ks.barnehagelister.rest

import no.nav.familie.ks.barnehagelister.domene.Barnehagelister
import no.nav.familie.ks.barnehagelister.kontrakt.SkjemaV1
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisterRepository
import no.nav.familie.prosessering.internal.TaskService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.UUID

@ProtectedWithClaims(issuer = "maskinporten", claimMap = ["scope=nav:familie/v1/kontantstotte/barnehagelister"])
@RestController
@Validated
@RequestMapping("/api/barnehagelister")
class BarnehagelisterController(
    private val barnehagelisterRepository: BarnehagelisterRepository,
    private val taskService: TaskService,
) {
    private val logger = LoggerFactory.getLogger(BarnehagelisterController::class.java)

    @PostMapping(path = ["/v1"])
    @Unprotected
    fun mottaBarnehagelister(
        @RequestBody skjemaV1: SkjemaV1,
    ): ResponseEntity<BarnehagelisteResponse> {
        logger.info("Mottok skjema")

        val liste = barnehagelisterRepository.findByIdOrNull(skjemaV1.id)
        return if (liste == null) {
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
            val httpStatusKode = if (liste.ferdigTid == null) HttpStatus.ACCEPTED else HttpStatus.OK
            ResponseEntity.status(httpStatusKode).body(
                BarnehagelisteResponse(
                    id = skjemaV1.id,
                    status = liste.status,
                    mottattTid = liste.opprettetTid,
                    ferdigTid = liste.ferdigTid,
                    links =
                        ResponseLinker(
                            status = "/api/barnehagelister/status/${skjemaV1.id}",
                        ),
                ),
            )
        }
    }

    @GetMapping(path = ["/status/{transaksjonsId}"])
    @Unprotected
    fun status(
        @PathVariable transaksjonsId: UUID,
    ): ResponseEntity<BarnehagelisteResponse> {
        logger.info("Mottok status")
        val liste = barnehagelisterRepository.findByIdOrNull(transaksjonsId)
        return if (liste == null) {
            ResponseEntity.notFound().build()
        } else {
            val httpStatusKode = if (liste.ferdigTid == null) HttpStatus.ACCEPTED else HttpStatus.OK
            ResponseEntity.status(httpStatusKode).body(
                BarnehagelisteResponse(
                    id = transaksjonsId,
                    status = liste.status,
                    mottattTid = liste.opprettetTid,
                    ferdigTid = liste.ferdigTid,
                    links =
                        ResponseLinker(
                            status = "/api/barnehagelister/status/$transaksjonsId",
                        ),
                ),
            )
        }
    }

    @GetMapping(path = ["/ping"])
    fun ping(): ResponseEntity<String> {
        logger.info("Mottok ping")
        return ResponseEntity.ok("pong")
    }

    data class BarnehagelisteResponse(
        val id: UUID,
        val status: String,
        val mottattTid: LocalDateTime,
        val ferdigTid: LocalDateTime?,
        val links: ResponseLinker,
    )

    data class ResponseLinker(
        val status: String,
    )
}
