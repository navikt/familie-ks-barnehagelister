package no.nav.familie.ks.barnehagelister.kontrakt

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

// TODO sanitize JSON input according to OWASP
@Schema(description = "Model for submitting kindergarten lists")
data class FormV1(
    @Schema(
        description = "Identifier for the submission, generated by the submitter",
        example = "19375e59-0f07-4c9b-a7bb-6f30fb43819b",
    )
    @field:NotNull
    val id: UUID,
    @field:Valid
    val kindergartens: List<Kindergarten>?,
    @field:NotNull
    @field:Valid
    val listInformation: ListInformation,
)

data class ListInformation(
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

data class Kindergarten(
    @Schema(description = "Name of the kindergarten")
    @field:NotBlank
    @field:Size(min = 1, max = 200)
    val name: String,
    @Schema(description = "Kindergarten's organization number")
    @field:Size(min = 9, max = 9, message = "Organization number must have 9 digits")
    @field:Pattern(regexp = "^[0-9]+(\\.[0-9]+)?$", message = "Organization number must be a numeric field")
    @field:NotBlank
    val organizationNumber: String,
    @Schema(description = "Kindergarten's address")
    @field:Valid
    val address: Address?,
    @Schema(
        description = "All children assigned a place in the kindergarten during the relevant period",
    )
    @field:Valid
    val childrenInformation: List<ChildInformation>,
)

data class ChildInformation(
    @Schema(
        description = "Number of hours per week it is agreed upon that the child will stay in the kindergarten",
        example = "37.5",
    )
    val agreedHoursInKindergarten: Double,
    @Schema(
        description = "Start date in the kindergarten",
    )
    val startDate: LocalDate,
    @Schema(
        description = "Date when the child leaves the kindergarten, null if the end date is not set",
    )
    val endDate: LocalDate?,
    @Schema(
        description = "Information about the child",
    )
    @field:Valid
    val child: PersonDTO,
    @Schema(
        description = "The person(s) the child lives with; if the child lives in an institution, the field is empty",
    )
    @field:Valid
    val guardians: List<PersonDTO>?,
)

data class PersonDTO(
    @Schema(
        description = "Identifier for the person, i.e., SSN or D-number",
        type = "String",
        example = "12345678910",
        requiredMode = REQUIRED,
    )
    @field:NotBlank
    @field:Size(min = 11, max = 11, message = "Social Security Number must have 11 digits")
    @field:Pattern(regexp = "^[0-9]+(\\.[0-9]+)?$", message = "Social Security Number must be a numeric field")
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
    @Schema(
        description = "Address for where the child lives. If the address is confidential it should be ommited.",
    )
    val address: Address?,
    @Schema(
        description = "Whether the address is confidential or not",
        example = "false",
    )
    val confidentialAddress: Boolean = false,
) {
    @AssertTrue(message = "Must either have an address or be a confidential adress")
    private fun isAddressOrConfidentialAddress() = (address != null && !confidentialAddress) || (address == null && confidentialAddress)
}

data class Address(
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
