package no.nav.familie.ks.barnehagelister.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import no.nav.familie.ks.barnehagelister.kontrakt.FormV1
import no.nav.familie.ks.barnehagelister.kontrakt.KindergartenlistResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import java.util.UUID

@ProtectedWithClaims(
    issuer = "maskinporten",
    claimMap = ["scope=nav:familie/v1/kontantstotte/barnehagelister"],
)
@RequestMapping("/api/kindergartenlists")
interface BarnehagelisterController {
    @Operation(summary = "Send in kindergarten list", operationId = "receiveKindergartenList")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "202",
                description = "Received and under processing",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = KindergartenlistResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "200",
                description = "Done processing",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = KindergartenlistResponse::class),
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
        @Valid @RequestBody formV1: FormV1,
        bindingResult: BindingResult,
        request: HttpServletRequest,
    ): ResponseEntity<KindergartenlistResponse>

    @Operation(summary = "Get status for submitted kindergartnen list")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Done processing",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = KindergartenlistResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "202",
                description = "Processing",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = KindergartenlistResponse::class),
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
    ): ResponseEntity<KindergartenlistResponse>

    @GetMapping(
        path = ["/ping"],
        produces = ["application/json;charset=UTF-8"],
    )
    fun ping(): String
}
