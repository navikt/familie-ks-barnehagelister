package no.nav.familie.ks.barnehagelister.rest

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import no.nav.familie.ks.barnehagelister.domene.Barnehagelister
import no.nav.familie.ks.barnehagelister.kontrakt.SkjemaV1
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisterRepository
import no.nav.security.token.support.core.api.ProtectedWithClaims
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
) {
    private val logger = LoggerFactory.getLogger(BarnehagelisterController::class.java)

    @Operation(summary = "Send inn barnehagelister")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "202",
                description = "Mottatt og under behandling",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = BarnehagelisteResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "200",
                description = "Ferdig prosessert",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = BarnehagelisteResponse::class),
                    ),
                ],
            ),
            ApiResponse(responseCode = "400", description = "Ugyldig format"),
            ApiResponse(responseCode = "500", description = "Intern feil"),
        ],
    )
    @PostMapping(
        path = ["/v1"],
        produces = ["application/json;charset=UTF-8"],
        consumes = ["application/json;charset=UTF-8"],
    )
    fun mottaBarnehagelister(
        @RequestBody skjemaV1: SkjemaV1,
    ): ResponseEntity<BarnehagelisteResponse> {
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

    @Operation(summary = "Hent status for innsendt barnehageliste")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Ferdig prosessert",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = BarnehagelisteResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "202",
                description = "Prosesseres",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = BarnehagelisteResponse::class),
                    ),
                ],
            ),
            ApiResponse(responseCode = "404", description = "Ikke funnet"),
            ApiResponse(responseCode = "500", description = "Intern feil"),
        ],
    )
    @GetMapping(
        path = ["/status/{transaksjonsId}"],
        produces = ["application/json;charset=UTF-8"],
    )
    fun status(
        @PathVariable transaksjonsId: UUID,
    ): ResponseEntity<BarnehagelisteResponse> {
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

    @GetMapping(
        path = ["/ping"],
        produces = ["application/json;charset=UTF-8"],
    )
    fun ping(): String {
        logger.info("Mottok ping")
        error("Ping feilet")
        return "\"OK\""
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

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonPropertyOrder(value = ["melding", "path", "timestamp", "status", "error", "transaksjonsId"])
    data class EksternTjenesteFeil(
        val path: String,
        val status: Int = HttpStatus.INTERNAL_SERVER_ERROR.value(),
        var error: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
        val melding: String,
        val callId: String,
        val timestamp: LocalDateTime = LocalDateTime.now(),
    )
}
