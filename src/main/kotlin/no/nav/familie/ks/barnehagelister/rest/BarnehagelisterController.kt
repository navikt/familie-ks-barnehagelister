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
import java.time.LocalDateTime
import java.util.UUID

@ProtectedWithClaims(
    issuer = "maskinporten",
    claimMap = ["scope=nav:familie/v1/kontantstotte/barnehagelister", "supplier=*"],
)
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
                        mediaType = "application/json",
                        schema = Schema(implementation = Void::class),
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
    val status: BarnehagelisteStatus,
    val mottattTid: LocalDateTime,
    val ferdigTid: LocalDateTime?,
    val links: ResponseLinker,
)

enum class BarnehagelisteStatus {
    MOTTATT,
    FERDIG,
}

data class ResponseLinker(
    val status: String,
)

class ValideringsfeilException(
    errors: List<ValideringsfeilInfo>,
) : RuntimeException("Valideringsfeil") {
    val errors = errors
}

@Schema(
    description = "Informasjon om valideringsfeil",
)
data class ValideringsfeilInfo(
    @Schema(
        description = "Hvilket felt som har valideringsfeil. Hvis info er ukjent, så er feltet satt til mangler",
        example = "barnehager[0].navn",
    )
    val parameter: String,
    @Schema(
        description = "Detalj om valideringsfeilen. Hvis man ikke har mer informasjon, så er feltet satt til mangler",
        example = "must not be blank",
    )
    val detail: String,
)

@Schema(description = "Problem Details med callId og errors. Basert på RFC 9457")
class ProblemDetailMedCallIdOgErrors(
    @Schema(
        description = "HTTP statuskode",
        example = "400",
    )
    val status: Int,
    @Schema(
        description = "En kort beskrivelse av feilen",
        example = "Bad request",
    )
    val title: String,
    @Schema(
        description = "En lesbar beskrivelse av feilen",
        example = "field must not be null",
    )
    val detail: String,
    @Schema(
        description = "En URI referanse til endepunktet hvor feilen oppstod",
        example = "/api/barnehagelister/v1",
    )
    val instance: String,
    @Schema(
        description = "En URI referanse til en spesifikk feiltype beskrevet på https://problems-registry.smartbear.com/",
        example = "https://problems-registry.smartbear.com/validation-error/",
    )
    val type: String,
    @Schema(
        description = "En identifikator for feilen som kan brukes til å spore feilen ved senere henvendelser",
        example = "57cf57cf06d84cc5883fc0a0a8804a7f",
    )
    val callId: String,
    val errors: List<ValideringsfeilInfo>?,
)
