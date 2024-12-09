package no.nav.familie.ks.barnehagelister.rest

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    name = "ProblemDetailWithCallIdAndErrors",
    description = "Problem Details with callId and errors. Based on RFC 9457",
)
class ProblemDetailMedCallIdOgErrors(
    @Schema(
        description = "HTTP statuscode",
        example = "400",
    )
    val status: Int,
    @Schema(
        description = "A short description of the error",
        example = "Bad request",
    )
    val title: String,
    @Schema(
        description = "A readable description of the error",
        example = "field must not be null",
    )
    val detail: String,
    @Schema(
        description = "A URI reference to the endpoint where the error occurred",
        example = "/api/kindergartenlists/v1",
    )
    val instance: String,
    @Schema(
        description = "A URI reference to a specific error type described at https://problems-registry.smartbear.com/",
        example = "https://problems-registry.smartbear.com/validation-error/",
    )
    val type: String,
    @Schema(
        description = "An identifier for the error that can be used to track the error in later inquiries",
        example = "57cf57cf06d84cc5883fc0a0a8804a7f",
    )
    val callId: String,
    val errors: List<ValideringsfeilInfo>?,
)
