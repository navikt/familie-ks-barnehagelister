package no.nav.familie.ks.barnehagelister.kontrakt

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

@Schema(description = "Modell for innsending av barnehagelister")
data class SkjemaV1(
    @Schema(
        description = "identifikator for innsendingen, generert av innsender",
        example = "19375e59-0f07-4c9b-a7bb-6f30fb43819b",
    )
    val id: UUID,
    @field:Valid val barnehager: List<Barnehage>,
    @field:NotNull @field:Valid val listeopplysninger: Listeopplysninger,
)

data class Listeopplysninger(
    @field:NotBlank val kommunenavn: String,
    @field:NotBlank val kommunenummer: String,
    @Schema(type = "string", format = "yearmonth", example = "2024-09") val innsendingGjelderArManed: YearMonth,
)

data class Barnehage(
    @Schema(
        description = "Barnehagens navn",
    )
    @field:NotBlank val navn: String,
    @Schema(
        description = "Barnehagens organisasjonsnummer",
    )
    @field:NotBlank val organisasjonsnummer: String,
    @Schema(
        description = "Barnehagens adresse",
    )
    @field:Valid val adresse: Adresse?,
    @Schema(
        description = "Alle barn som er tildelt barnehageplass i barnehagen i gjeldende periode",
    )
    @field:Valid val barnInfolinjer: List<BarnInfolinje>,
)

data class BarnInfolinje(
    @Schema(
        description = "Antall timer barnet har avtalt oppholdstid i barnehagen per uke",
        example = "37,5",
    )
    val avtaltOppholdstidTimer: Double,
    @Schema(
        description = "Dato for oppstart i barnehagen",
    )
    val startdato: LocalDate,
    @Schema(
        description = "Dato for når barnet slutter i barnehagen, er null hvis sluttdato ikke er satt",
    )
    val sluttdato: LocalDate?,
    @Schema(
        description = "Info om barnet",
    )
    val barn: Person,
    @Schema(
        description = "Den eller de barnet bor hos, bor barnet på institusjon er feltet tomt",
    )
    val foresatte: List<Person>?,
    @Schema(
        description = "Endringstype ",
    )
    val endringstype: Endringstype,
)

enum class Endringstype {
    STARTET,
    ENDRET,
    INGEN_ENDRING,
    SLUTTET,
}

data class Person(
    @Schema(
        description = "Ident for person, dvs. fnr eller dnr",
        type = "String",
        example = "12345678910",
        requiredMode = REQUIRED,
    )
    @field:NotBlank val fodselsnummer: String,
    @Schema(
        description = "Fornavn",
        example = "Ola",
        requiredMode = REQUIRED,
    )
    @Size(max = 200, message = "Fornavn kan være maks 200 tegn.")
    @field:NotBlank val fornavn: String,
    @Schema(
        description = "Etternavn",
        example = "Nordmann",
        requiredMode = REQUIRED,
    )
    @Size(max = 200, message = "Etternavn kan være maks 200 tegn.")
    @field:NotBlank val etternavn: String,
    @field:Valid val adresse: Adresse?,
)

data class Adresse(
    @Schema(
        description = """Bruksenhetsnummer identifiserer en boligenhet innenfor et 
                         bygg eller en bygningsdel. Bokstaven H, L, U eller K etterfult av 4 siffer""",
        example = "H0101",
    )
    @Pattern(regexp = "(?i)^[HULK][0-9]{4}$|^$", message = "H, L, U eller K etterfult av 4 siffer")
    val bruksenhetsnummer: String?,
    @Schema(
        description = "Veinavn og husnummer",
        example = "Svingen 1",
    )
    val adresselinje1: String?,
    @Schema(
        description = "Alternativ adresse",
        example = "Postboks 123",
    )
    val adresselinje2: String?,
    @Schema(
        description = "Norsk postnummer, fire siffer",
        example = "0102",
    )
    @field:NotBlank val postnummer: String,
    @Schema(
        description = "Norsk poststed",
    )
    @field:NotBlank val poststed: String,
)
