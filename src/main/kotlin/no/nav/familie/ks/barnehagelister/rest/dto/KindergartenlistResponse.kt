package no.nav.familie.ks.barnehagelister.rest.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.ResponseEntity
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID

data class KindergartenlistResponse(
    val id: UUID,
    val status: BarnehagelisteStatusEngelsk,
    val receivedTime: LocalDateTime,
    val finishedTime: LocalDateTime?,
    val warnings: List<EtterprosesseringfeilInfo> = emptyList(),
    val links: ResponseLinksResponseDto,
)

@Schema(description = "A URI reference to endpoint to get the status for the submitted kindergarten list", name = "ResponseLinks")
data class ResponseLinksResponseDto(
    val status: URI,
)

@Schema(
    name = "ValidationWarnings",
    description =
        "Validation warnings on inconsistencies within the kindergarten list. " +
            "Will be empty until status = DONE.",
)
data class EtterprosesseringfeilInfo(
    @Schema()
    val type: EtterprosesseringfeilType,
    @Schema(
        description = "Details about the validation error. If no more information is available, the field is set to missing",
        example = "Overlapping period within the same list for children. child=12345678901",
    )
    val detail: String,
)

enum class EtterprosesseringfeilType {
    OVERLAPPING_PERIOD_WITHIN_SAME_LIST,
}

enum class BarnehagelisteStatus(
    val engelsk: BarnehagelisteStatusEngelsk,
) {
    MOTTATT(BarnehagelisteStatusEngelsk.RECEIVED),
    FERDIG(BarnehagelisteStatusEngelsk.DONE),
}

enum class BarnehagelisteStatusEngelsk {
    RECEIVED,
    DONE,
}

fun KindergartenlistResponse.toResponseEntity() =
    if (finishedTime == null) {
        ResponseEntity.accepted().body(this)
    } else {
        ResponseEntity.ok().body(this)
    }
