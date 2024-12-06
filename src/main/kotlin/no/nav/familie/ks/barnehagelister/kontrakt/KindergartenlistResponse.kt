package no.nav.familie.ks.barnehagelister.kontrakt

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class KindergartenlistResponse(
    val id: UUID,
    val status: BarnehagelisteStatusEngelsk,
    val receivedTime: LocalDateTime,
    val finishedTime: LocalDateTime?,
    val links: ResponseLinks,
)

@Schema(description = "A URI reference to endpoint to get the status for the submitted kindergarten list")
data class ResponseLinks(
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
