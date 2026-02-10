package no.nav.familie.ks.barnehagelister.rest

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.ProblemDetail

@Schema(
    name = "ProblemDetailWithCallIdAndErrors",
    description = "Problem Details with callId and errors. Based on RFC 9457",
)
@JsonInclude(JsonInclude.Include.NON_NULL)
class ProblemDetailMedCallIdOgErrors(
    callId: String,
) : ProblemDetail() {
    @Schema(
        description = "An identifier for the error that can be used to track the error in later inquiries",
        example = "57cf57cf06d84cc5883fc0a0a8804a7f",
    )
    val callId: String = callId

    @Schema(
        description = "List of validation errors",
    )
    var errors: List<JsonValideringsfeilInfo>? = null
}
