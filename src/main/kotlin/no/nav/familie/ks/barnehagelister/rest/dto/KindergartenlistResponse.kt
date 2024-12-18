package no.nav.familie.ks.barnehagelister.rest.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime
import java.util.UUID

data class KindergartenlistResponse(
    val id: UUID,
    val status: BarnehagelisteStatusEngelsk,
    val receivedTime: LocalDateTime,
    val finishedTime: LocalDateTime?,
    val links: ResponseLinksResponseDto,
)

@Schema(description = "A URI reference to endpoint to get the status for the submitted kindergarten list", name = "ResponseLinks")
data class ResponseLinksResponseDto(
    val status: String,
)

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
        ResponseEntity.ok().build()
    }
