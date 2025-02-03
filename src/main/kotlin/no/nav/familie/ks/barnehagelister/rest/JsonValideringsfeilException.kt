package no.nav.familie.ks.barnehagelister.rest

import io.swagger.v3.oas.annotations.media.Schema

class JsonValideringsfeilException(
    val errors: List<JsonValideringsfeilInfo>,
) : RuntimeException("Validation error")

@Schema(
    name = "JsonValidationErrorInformation",
    description = "Information about JSON validation errors",
)
data class JsonValideringsfeilInfo(
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
