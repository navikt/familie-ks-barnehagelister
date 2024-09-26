package no.nav.familie.ks.barnehagelister.domene

import no.nav.familie.ks.barnehagelister.kontrakt.Skjema
import org.springframework.data.annotation.Id
import java.util.UUID

data class Barnehagelister(
    @Id
    val id: UUID,
    val rawJson: Skjema ,
    val status: String
)