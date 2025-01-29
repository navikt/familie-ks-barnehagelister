package no.nav.familie.ks.barnehagelister.rest.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import java.time.LocalDate

@Schema(name = "ChildInformation")
data class ChildInformationRequestDto(
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
    val child: PersonRequestDto,
    @Schema(
        description = "The person(s) the child lives with; if the child lives in an institution, the field is empty",
    )
    @field:Valid
    val guardians: List<PersonRequestDto>?,
)
