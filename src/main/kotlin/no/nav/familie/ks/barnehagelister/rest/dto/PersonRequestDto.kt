package no.nav.familie.ks.barnehagelister.rest.dto

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import no.nav.familie.ks.barnehagelister.domene.Person
import no.nav.familie.ks.barnehagelister.validering.Fødselsnummer

@Schema(name = "Person")
data class PersonRequestDto(
    @Schema(
        description = "Identifier for the person, i.e., SSN or D-number",
        type = "String",
        example = "12345678910",
        requiredMode = REQUIRED,
    )
    @field:NotBlank
    @field:Size(min = 11, max = 11, message = "Social Security Number must have 11 digits")
    @field:Fødselsnummer
    val socialSecurityNumber: String,
    @Schema(
        description = "First name",
        example = "Ola",
        requiredMode = REQUIRED,
    )
    @field:Size(min = 1, max = 200, message = "First name can be a maximum of 200 characters.")
    @field:NotBlank
    val firstName: String,
    @Schema(
        description = "Last name",
        example = "Nordmann",
        requiredMode = REQUIRED,
    )
    @field:NotBlank
    @field:Size(min = 1, max = 200, message = "Last name can be a maximum of 200 characters.")
    @field:NotBlank
    val lastName: String,
    @field:Valid
    val address: AddressRequestDto?,
)

fun PersonRequestDto.mapTilPerson(): Person =
    Person(
        fodselsnummer = this.socialSecurityNumber,
        fornavn = this.firstName,
        etternavn = this.lastName,
        adresse = this.address?.mapTilAdresse(),
    )
