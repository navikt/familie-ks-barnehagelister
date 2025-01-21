package no.nav.familie.ks.barnehagelister.kafka

import java.time.LocalDate
import java.util.UUID

class BarnehagebarnKS(
    val id: UUID,
    val ident: String,
    val fom: LocalDate,
    val tom: LocalDate? = null,
    val antallTimerIBarnehage: Double,
    val kommuneNavn: String,
    val kommuneNr: String,
    val barnehagelisteId: UUID,
)