package no.nav.familie.ks.barnehagelister.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import no.nav.familie.ks.barnehagelister.kontrakt.SkjemaV1
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID

@ProtectedWithClaims(issuer = "maskinporten", claimMap = ["scope=nav:familie/v1/kontantstotte/barnehagelister"])
@RequestMapping("/api/barnehagelister")
interface BarnehagelisterController {
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
            ApiResponse(
                responseCode = "400",
                description = "Ugyldig format",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = ProblemDetailMedCallIdOgErrors::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = ProblemDetailMedCallIdOgErrors::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Intern feil",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = ProblemDetailMedCallIdOgErrors::class),
                    ),
                ],
            ),
        ],
    )
    @PostMapping(
        path = ["/v1"],
        produces = ["application/json;charset=UTF-8"],
        consumes = ["application/json;charset=UTF-8"],
    )
    fun mottaBarnehagelister(
        @Valid @RequestBody skjemaV1: SkjemaV1,
        bindingResult: BindingResult,
    ): ResponseEntity<BarnehagelisteResponse>

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
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = ProblemDetailMedCallIdOgErrors::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Ikke funnet",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = ProblemDetailMedCallIdOgErrors::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Intern feil",
                content = [
                    Content(
                        mediaType = "application/problem+json",
                        schema = Schema(implementation = ProblemDetailMedCallIdOgErrors::class),
                    ),
                ],
            ),
        ],
    )
    @GetMapping(
        path = ["/status/{transaksjonsId}"],
        produces = ["application/json;charset=UTF-8"],
    )
    fun status(
        @PathVariable transaksjonsId: UUID,
    ): ResponseEntity<BarnehagelisteResponse>

    @GetMapping(
        path = ["/ping"],
        produces = ["application/json;charset=UTF-8"],
    )
    fun ping(): String
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

class ValideringsfeilException(
    errors: List<ValideringsfeilInfo>,
) : RuntimeException() {
    val errors = errors
}

data class ValideringsfeilInfo(
    val parameter: String,
    val detail: String,
)

class ProblemDetailMedCallIdOgErrors(
    val status: Int,
    val title: String,
    val detail: String,
    val instance: String,
    val type: URI,
    val callId: String,
    val errors: List<ValideringsfeilInfo>?,
)
