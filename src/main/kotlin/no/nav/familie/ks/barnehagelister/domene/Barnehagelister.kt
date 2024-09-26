package no.nav.familie.ks.barnehagelister.domene

import no.nav.familie.ks.barnehagelister.kontrakt.SkjemaV1
import org.springframework.data.annotation.Id
import java.util.UUID

data class Barnehagelister(
    @Id
    val id: UUID,
    val rawJson: SkjemaV1,
    val status: String,
)
