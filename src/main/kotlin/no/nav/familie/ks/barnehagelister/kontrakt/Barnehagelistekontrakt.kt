package no.nav.familie.ks.barnehagelister.kontrakt

import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

data class Skjema(
    val id: UUID,
    val barnInfolinjer: List<BarnInfolinje>,
    val listeopplysninger: Listeopplysninger,
)

data class Listeopplysninger(
    val kommuneNavn: String,
    val kommunenummer: String,
    val innsendingGjelder: YearMonth,
)

data class BarnInfolinje(
    val avtaltOppholdstidTimer: Number,
    val startdato: LocalDate,
    val sluttdato: LocalDate?,
    val barn: Person,
    val foreldre: List<Person>,
    val barnehage: Barnehage,
    val endringstype: String,
)

data class Barnehage(
    val navn: String,
    val organisasjonsnummer: String,
    val adresse: Adresse?,
)

data class Person(
    val fodselsnummer: String,
    val navn: String,
    val adresse: Adresse?,
)

data class Adresse(
    val adresselinje1: String?,
    val adresselinje2: String?,
    val postnummer: String,
    val poststed: String,
)
