package no.nav.familie.ks.barnehagelister.domene

import java.util.UUID

// TODO sanitize JSON input according to OWASP
data class SkjemaV1(
    val id: UUID,
    val barnehager: List<Barnehage>?,
    val listeopplysninger: Listeopplysninger,
)
