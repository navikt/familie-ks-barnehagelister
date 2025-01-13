package no.nav.familie.ks.barnehagelister.kafka

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDate
import java.util.UUID

data class Barnehagebarn(
    @Id
    val id: UUID = UUID.randomUUID(),
    val ident: String,
    val fom: LocalDate,
    val tom: LocalDate? = null,
    val antallTimerIBarnehage: Double,
    val kommuneNavn: String,
    val kommuneNr: String,
    @Column("fk_barnehagelister_id")
    val barnehagelisteId: UUID,
    val organisasjonsnummer: String,
)
