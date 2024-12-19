package no.nav.familie.ks.barnehagelister.kafka

import java.time.LocalDate
import java.util.UUID

data class BarnehageBarnKS(
    val id: UUID = UUID.randomUUID(),
    var ident: String,
    var fom: LocalDate,
    var tom: LocalDate? = null,
    var antallTimerIBarnehage: Double,
    var kommuneNavn: String,
    var kommuneNr: String,
    var arkivReferanse: String,
)
