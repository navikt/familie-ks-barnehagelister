package no.nav.familie.ks.barnehagelister.domene

import no.nav.familie.ks.barnehagelister.rest.dto.BarnehagelisteStatus
import no.nav.familie.ks.barnehagelister.rest.dto.KindergartenlistResponse
import no.nav.familie.ks.barnehagelister.rest.dto.ResponseLinksResponseDto
import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import java.util.UUID

data class Barnehageliste(
    @Id
    val id: UUID,
    val rawJson: SkjemaV1,
    val status: BarnehagelisteStatus,
    val opprettetTid: LocalDateTime = LocalDateTime.now(),
    val ferdigTid: LocalDateTime? = null,
    val leverandorOrgNr: String? = null,
    val kommuneOrgNr: String? = null,
) {
    override fun toString(): String =
        "Barnehageliste(id=$id, status=$status, opprettetTid=$opprettetTid, ferdigTid=$ferdigTid, leverandorOrgNr=$leverandorOrgNr, kommuneOrgNr=$kommuneOrgNr)"
}

fun Barnehageliste.tilKindergartenlistResponse() =
    KindergartenlistResponse(
        id = rawJson.id,
        status = BarnehagelisteStatus.MOTTATT.engelsk,
        receivedTime = opprettetTid,
        finishedTime = ferdigTid,
        links =
            ResponseLinksResponseDto(
                status = "/api/kindergartenlists/status/${rawJson.id}",
            ),
    )
