package no.nav.familie.ks.barnehagelister.domene

import no.nav.familie.ks.barnehagelister.rest.BarnehagelisteStatus
import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import java.util.UUID

data class Barnehagelister(
    @Id
    val id: UUID,
    val rawJson: SkjemaV1,
    val status: BarnehagelisteStatus,
    val opprettetTid: LocalDateTime = LocalDateTime.now(),
    val ferdigTid: LocalDateTime? = null,
) {
    fun erFerdigProsessert(): Boolean = ferdigTid != null
}
