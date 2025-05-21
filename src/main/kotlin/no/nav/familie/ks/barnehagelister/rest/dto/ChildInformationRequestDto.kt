package no.nav.familie.ks.barnehagelister.rest.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import java.time.LocalDate

@Schema(
    name = "ChildInformation",
    description =
        "Information concerning a child during a given period of time. May be several within the same list for" +
            " the same child if anything changes. F.eks. If a child has agreed to go 20 hours in kindergarten " +
            "1. may - 15. may, 25 hours 16. may - 31. may and 30 hours from 1. june, then those should be sent as" +
            " three different entries.",
)
data class ChildInformationRequestDto(
    @Schema(
        description = "Number of hours per week it is agreed upon that the child will stay in the kindergarten during this period of time.",
        example = "37.5",
    )
    val agreedHoursInKindergarten: Double,
    @Schema(
        description = "Start date for this period in the kindergarten.",
    )
    val startDate: LocalDate,
    @Schema(
        description = "End date for this period in the kindergarten, null if it is the current period/the end date is not known.",
    )
    val endDate: LocalDate?,
    @Schema(
        description = "Information about the child",
    )
    @field:Valid
    val child: PersonRequestDto,
    @Schema(
        description = "The person(s) the child lives with; if the child lives in an institution, the field is empty",
    )
    @field:Valid
    val guardians: List<PersonRequestDto>?,
)
