package no.nav.familie.ks.barnehagelister.domene

import no.nav.familie.ks.barnehagelister.kontrakt.SkjemaV1
import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import java.util.UUID

data class Barnehagelister(
    @Id
    val id: UUID,
    val rawJson: SkjemaV1,
    val status: String,
    val opprettetTid: LocalDateTime = LocalDateTime.now(),
    val ferdigTid: LocalDateTime? = null,
) {
    fun erFerdigProsessert(): Boolean = ferdigTid != null
}
