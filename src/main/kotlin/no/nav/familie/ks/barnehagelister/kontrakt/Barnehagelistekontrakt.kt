package no.nav.familie.ks.barnehagelister.kontrakt

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

data class SkjemaV1(
    val id: UUID,
    val barnehager: List<Barnehage>?,
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
    val foresatte: List<Person>,
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
    @Schema(
        description = """Bruksenhetsnummer identifiserer en boligenhet innenfor et 
                         bygg eller en bygningsdel. Bokstaven H, L, U eller K etterfult av 4 siffer""",
        example = "H0101",
    )
    @Pattern(regexp = "(?i)^[HULK][0-9]{4}$|^$", message = "H, L, U eller K etterfult av 4 siffer")
    val bruksenhetsnummer: String?,
    val adresselinje1: String?,
    val adresselinje2: String?,
    @Schema(
        description = "Norsk postnummer, fire siffer",
        example = "0102",
    )
    val postnummer: String,
    val poststed: String,
)
