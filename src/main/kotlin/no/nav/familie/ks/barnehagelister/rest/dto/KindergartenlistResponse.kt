package no.nav.familie.ks.barnehagelister.rest.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime
import java.util.UUID

data class KindergartenlistResponse(
    val id: UUID,
    @Schema(
        description = "\"Received\" until further validations are done. \"Done\" when the list is processed, even if there are warnings.",
    )
    val status: BarnehagelisteStatusEngelsk,
    val receivedTime: LocalDateTime,
    val finishedTime: LocalDateTime?,
    val links: ResponseLinksResponseDto,
)

@Schema(
    description = "A URI reference to endpoint to get the status for the submitted kindergarten list",
    name = "ResponseLinks",
)
data class ResponseLinksResponseDto(
    @Schema(example = "/api/kindergartenlists/status/19375e59-0f07-4c9b-a7bb-6f30fb43819b")
    val status: String,
    @Schema(
        description =
            "Heavier validations that fail will be listed here. List is still processed if there are warnings," +
                " but gives an indication of faulty data.",
    )
    val warnings: List<EtterprosesseringfeilInfo> = emptyList(),
)

@Schema(name = "ValidationWarnings")
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
