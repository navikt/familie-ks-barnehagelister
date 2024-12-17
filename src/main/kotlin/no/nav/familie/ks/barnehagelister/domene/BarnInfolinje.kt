package no.nav.familie.ks.barnehagelister.domene

import java.time.LocalDate

data class BarnInfolinje(
    val avtaltOppholdstidTimer: Double,
    val startdato: LocalDate,
    val sluttdato: LocalDate?,
    val foresatte: List<Person>?,
)
