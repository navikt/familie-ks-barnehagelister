package no.nav.familie.ks.barnehagelister.kafka

import org.springframework.data.annotation.Id
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
    val arkivReferanse: String,
    val organisasjonsnummer: String,
)
