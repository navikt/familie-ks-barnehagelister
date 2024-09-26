package no.nav.familie.ks.barnehagelister.rest

import no.nav.familie.ks.barnehagelister.domene.Barnehagelister
import no.nav.familie.ks.barnehagelister.kontrakt.SkjemaV1
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisterRepository
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@ProtectedWithClaims(issuer = "maskinporten", claimMap = ["scope=nav:familie/v1/kontantstotte/barnehagelister"])
@RestController
@Validated
@RequestMapping("/api/barnehagelister")
class BarnehagelisterController(
    private val barnehagelisterRepository: BarnehagelisterRepository,
) {
    private val logger = LoggerFactory.getLogger(BarnehagelisterController::class.java)

    @PostMapping(path = ["/v1"])
    @Unprotected
    fun mottaBarnehagelister(
        @RequestBody skjemaV1: SkjemaV1,
    ): ResponseEntity<String> {
        logger.info("Mottok skjema")
        barnehagelisterRepository.insert(Barnehagelister(skjemaV1.id, skjemaV1, "MOTTATT"))
        return ResponseEntity.accepted().body("ok")
    }

    @GetMapping(path = ["/status/{transaksjonsId}"])
    @Unprotected
    fun status(
        @PathVariable transaksjonsId: UUID,
    ): ResponseEntity<String> {
        logger.info("Mottok status")
        val status = barnehagelisterRepository.findByIdOrNull(transaksjonsId)?.status ?: "Ukjent"
        return ResponseEntity.ok(status)
    }

    @GetMapping(path = ["/ping"])
    fun ping(): ResponseEntity<String> {
        logger.info("Mottok ping")
        return ResponseEntity.ok("pong")
    }
}
