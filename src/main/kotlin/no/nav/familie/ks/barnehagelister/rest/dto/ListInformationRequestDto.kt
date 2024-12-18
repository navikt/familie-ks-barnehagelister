package no.nav.familie.ks.barnehagelister.rest.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.familie.ks.barnehagelister.domene.Listeopplysninger
import java.time.YearMonth

@Schema(name = "ListInformation", requiredMode = Schema.RequiredMode.REQUIRED)
data class ListInformationRequestDto(
    @field:NotBlank
    @field:Size(min = 1, max = 200)
    val municipalityName: String,
    @field:NotBlank
    @field:Size(min = 4, max = 4, message = "Municipality number must have 4 digits")
    @field:Pattern(regexp = "^[0-9]+(\\.[0-9]+)?$", message = "Municipality number must be a numeric field")
    val municipalityNumber: String,
    @Schema(type = "string", format = "yearmonth", example = "2024-09")
    val submissionForYearMonth: YearMonth,
)

fun ListInformationRequestDto.mapTilListeopplysninger(): Listeopplysninger =
    Listeopplysninger(
        kommunenavn = this.municipalityName,
        kommunenummer = this.municipalityNumber,
        innsendingGjelderArManed = this.submissionForYearMonth,
    )
