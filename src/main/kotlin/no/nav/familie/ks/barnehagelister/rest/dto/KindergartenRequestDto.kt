package no.nav.familie.ks.barnehagelister.rest.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import no.nav.familie.ks.barnehagelister.domene.Barnehage
import no.nav.familie.ks.barnehagelister.validering.Organisasjonsnummer

@Schema(name = "Kindergarten")
data class KindergartenRequestDto(
    @Schema(description = "Name of the kindergarten")
    @field:NotBlank
    @field:Size(min = 1, max = 200)
    val name: String,
    @Schema(
        description = "Kindergarten's organization number",
        example = "310028142",
    )
    @field:Organisasjonsnummer
    @field:NotBlank
    val organizationNumber: String,
    @Schema(description = "Kindergarten's address")
    @field:Valid
    val address: AddressRequestDto?,
    @Schema(
        description = "All children assigned a place in the kindergarten during the relevant period",
    )
    @field:Valid
    val childrenInformation: List<ChildInformationRequestDto>,
)

fun KindergartenRequestDto.mapTilBarnehage(): Barnehage =
    Barnehage(
        navn = this.name,
        organisasjonsnummer = this.organizationNumber,
        adresse = this.address?.mapTilAdresse(),
        barnInfolinjer = this.childrenInformation.map { it.mapTilBarnInfolinje() },
    )
