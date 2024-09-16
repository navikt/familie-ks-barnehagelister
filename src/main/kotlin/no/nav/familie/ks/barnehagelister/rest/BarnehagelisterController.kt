package no.nav.familie.ks.barnehagelister.rest

import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = "maskinporten", claimMap = ["scope=nav:familie/v1/kontantstotte/barnehagelister"])
@RestController
@Validated
@RequestMapping("/barnehagelister")
class BarnehagelisterController {
    private val logger = LoggerFactory.getLogger(BarnehagelisterController::class.java)

    @GetMapping
    fun ping(): ResponseEntity<String> {
        logger.info("Mottok ping")
        return ResponseEntity.ok("pong")
    }
}
