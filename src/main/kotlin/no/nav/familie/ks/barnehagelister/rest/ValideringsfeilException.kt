package no.nav.familie.ks.barnehagelister.rest

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "ValidationError")
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
        example = "kindergartens[0].navn",
    )
    val parameter: String,
    @Schema(
        description = "Details about the validation error. If no more information is available, the field is set to missing",
        example = "must not be blank",
    )
    val detail: String,
)
