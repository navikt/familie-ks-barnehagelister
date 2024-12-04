package no.nav.familie.ks.barnehagelister.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
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
import java.time.LocalDateTime
import java.util.UUID

@ProtectedWithClaims(
    issuer = "maskinporten",
    claimMap = ["scope=nav:familie/v1/kontantstotte/barnehagelister"],
)
@RequestMapping("/api/barnehagelister")
interface BarnehagelisterController {
    @Operation(summary = "Send in kindergarten list")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "202",
                description = "Received and under processing",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = BarnehagelisteResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "200",
                description = "Done processing",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = BarnehagelisteResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid format",
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
                description = "Internal error",
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
        request: HttpServletRequest,
    ): ResponseEntity<BarnehagelisteResponse>

    @Operation(summary = "Get status for submitted kindergartnen list")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Done processing",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = BarnehagelisteResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "202",
                description = "Processing",
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
                description = "Not found",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = Void::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal error",
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
        request: HttpServletRequest,
    ): ResponseEntity<BarnehagelisteResponse>

    @GetMapping(
        path = ["/ping"],
        produces = ["application/json;charset=UTF-8"],
    )
    fun ping(): String
}

data class BarnehagelisteResponse(
    val id: UUID,
    val status: BarnehagelisteStatusEngelsk,
    val mottattTid: LocalDateTime,
    val ferdigTid: LocalDateTime?,
    val links: ResponseLinker,
)

enum class BarnehagelisteStatus(
    val engelsk: BarnehagelisteStatusEngelsk,
) {
    MOTTATT(BarnehagelisteStatusEngelsk.RECEIVED),
    FERDIG(BarnehagelisteStatusEngelsk.DONE),
}

enum class BarnehagelisteStatusEngelsk {
    RECEIVED,
    DONE,
}

data class ResponseLinker(
    val status: String,
)

class ValideringsfeilException(
    val errors: List<ValideringsfeilInfo>,
) : RuntimeException("Validation error")

@Schema(
    name = "ValidationErrorInformation",
    description = "Information about validation errors",
)
data class ValideringsfeilInfo(
    @Schema(
        description = "Which field has validation errors. If the info is unknown, then the field is set to missing",
        example = "barnehager[0].navn",
    )
    val parameter: String,
    @Schema(
        description = "Details about the validation error. If no more information is available, the field is set to missing",
        example = "must not be blank",
    )
    val detail: String,
)

@Schema(
    name = "ProblemDetailWithCallIdOgErrors",
    description = "Problem Details with callId and errors. Based on RFC 9457",
)
class ProblemDetailMedCallIdOgErrors(
    @Schema(
        description = "HTTP statuscode",
        example = "400",
    )
    val status: Int,
    @Schema(
        description = "A short description of the error",
        example = "Bad request",
    )
    val title: String,
    @Schema(
        description = "A readable description of the error",
        example = "field must not be null",
    )
    val detail: String,
    @Schema(
        description = "A URI reference to the endpoint where the error occurred",
        example = "/api/barnehagelister/v1",
    )
    val instance: String,
    @Schema(
        description = "A URI reference to a specific error type described at https://problems-registry.smartbear.com/",
        example = "https://problems-registry.smartbear.com/validation-error/",
    )
    val type: String,
    @Schema(
        description = "An identifier for the error that can be used to track the error in later inquiries",
        example = "57cf57cf06d84cc5883fc0a0a8804a7f",
    )
    val callId: String,
    val errors: List<ValideringsfeilInfo>?,
)
