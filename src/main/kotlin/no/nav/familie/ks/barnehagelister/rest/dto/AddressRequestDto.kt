package no.nav.familie.ks.barnehagelister.rest.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.familie.ks.barnehagelister.domene.Adresse

data class AddressRequestDto(
    @Schema(
        description = """Unit number identifies a residential unit within a building or part of a building. 
                         The letter H, L, U, or K followed by 4 digits.""",
        example = "H0101",
    )
    @field:Pattern(regexp = "(?i)^[HULK][0-9]{4}$|^$", message = "H, L, U, or K followed by 4 digits")
    @field:Size(min = 5, max = 5, message = "Unit number must have 5 characters")
    val unitNumber: String?,
    @Schema(
        description = "Street name and house number",
        example = "Svingen 1",
    )
    @field:Size(min = 1, max = 200)
    val addressLine1: String?,
    @Schema(
        description = "Alternative address",
        example = "Postboks 123",
    )
    @field:Size(min = 1, max = 200)
    val addressLine2: String?,
    @Schema(
        description = "Norwegian zip code, four digits",
        example = "0102",
    )
    @field:NotBlank
    @field:Size(min = 4, max = 4, message = "Zip code must have 4 digits")
    @field:Pattern(regexp = "^[0-9]+(\\.[0-9]+)?$", message = "Zip code must be a numeric field")
    val zipCode: String,
    @Schema(
        description = "Norwegian city name",
    )
    @field:NotBlank
    val postalTown: String,
)

fun AddressRequestDto.mapTilAdresse(): Adresse =
    Adresse(
        bruksenhetsnummer = this.unitNumber,
        adresselinje1 = this.addressLine1,
        adresselinje2 = this.addressLine2,
        postnummer = this.zipCode,
        poststed = this.postalTown,
    )
