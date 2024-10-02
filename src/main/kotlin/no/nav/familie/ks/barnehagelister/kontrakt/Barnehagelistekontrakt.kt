package no.nav.familie.ks.barnehagelister.kontrakt

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

data class SkjemaV1(
    val id: UUID,
    val barnehager: List<Barnehage>,
    val listeopplysninger: Listeopplysninger,
)

data class Listeopplysninger(
    val kommunenavn: String,
    val kommunenummer: String,
    @Schema(type = "string", format = "yearmonth", example = "2024-09") val innsendingGjelderArManed: YearMonth,
)

data class Barnehage(
    val navn: String,
    val organisasjonsnummer: String,
    val adresse: Adresse?,
    val barnInfolinjer: List<BarnInfolinje>,
)

data class BarnInfolinje(
    val avtaltOppholdstidTimer: Double,
    val startdato: LocalDate,
    val sluttdato: LocalDate?,
    val barn: Person,
    val foreldre: List<Person>,
    val endringstype: Endringstype,
)

enum class Endringstype {
    STARTET,
    ENDRET,
    INGEN_ENDRING,
    SLUTTET,
}

data class Person(
    val fodselsnummer: String,
    val fornavn: String,
    val etternavn: String,
    val adresse: Adresse?,
)

data class Adresse(
    val adresselinje1: String?,
    val adresselinje2: String?,
    val postnummer: String,
    val poststed: String,
)
