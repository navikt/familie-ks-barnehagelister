package no.nav.familie.ks.barnehagelister.domene

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import java.util.UUID

data class BarnehagelisteValideringsfeil(
    @Id
    val id: UUID,
    @Column("fk_barnehageliste_id")
    val barnehagelisteId: UUID,
    val type: String,
    val feilinfo: String,
    val ident: String,
    val opprettetTid: LocalDateTime = LocalDateTime.now(),
) {
    override fun toString(): String =
        "BarnehagelisteValideringsfeil(id=$id, barnehagelisteId=$barnehagelisteId, type='$type' feilinfo='$feilinfo', opprettetTid=$opprettetTid)"
}
