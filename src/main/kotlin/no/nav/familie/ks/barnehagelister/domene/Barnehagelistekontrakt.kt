package no.nav.familie.ks.barnehagelister.domene

import no.nav.familie.ks.barnehagelister.kontrakt.Address
import no.nav.familie.ks.barnehagelister.kontrakt.ChildInformation
import no.nav.familie.ks.barnehagelister.kontrakt.FormV1
import no.nav.familie.ks.barnehagelister.kontrakt.Kindergarten
import no.nav.familie.ks.barnehagelister.kontrakt.ListInformation
import no.nav.familie.ks.barnehagelister.kontrakt.PersonDTO
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

// TODO sanitize JSON input according to OWASP
data class SkjemaV1(
    val id: UUID,
    val barnehager: List<Barnehage>?,
    val listeopplysninger: Listeopplysninger,
)

fun FormV1.mapTilSkjemaV1(): SkjemaV1 =
    SkjemaV1(
        id = this.id,
        barnehager = this.kindergartens?.map { it.mapTilBarnehage() },
        listeopplysninger = this.listInformation.mapTilListeopplysninger(),
    )

data class Listeopplysninger(
    val kommunenavn: String,
    val kommunenummer: String,
    val innsendingGjelderArManed: YearMonth,
)

fun ListInformation.mapTilListeopplysninger(): Listeopplysninger =
    Listeopplysninger(
        kommunenavn = this.municipalityName,
        kommunenummer = this.municipalityNumber,
        innsendingGjelderArManed = this.submissionForYearMonth,
    )

data class Barnehage(
    val navn: String,
    val organisasjonsnummer: String,
    val adresse: Adresse?,
    val barnInfolinjer: List<BarnInfolinje>,
)

fun Kindergarten.mapTilBarnehage(): Barnehage =
    Barnehage(
        navn = this.name,
        organisasjonsnummer = this.organizationNumber,
        adresse = this.address?.mapTilAdresse(),
        barnInfolinjer = this.childrenInformation.map { it.mapTilBarnInfolinje() },
    )

data class BarnInfolinje(
    val avtaltOppholdstidTimer: Double,
    val startdato: LocalDate,
    val sluttdato: LocalDate?,
    val foresatte: List<Person>?,
)

fun ChildInformation.mapTilBarnInfolinje(): BarnInfolinje =
    BarnInfolinje(
        avtaltOppholdstidTimer = this.agreedHoursInKindergarten,
        startdato = this.startDate,
        sluttdato = this.endDate,
        foresatte = this.guardians?.map { it.mapTilPerson() },
    )

data class Person(
    val fodselsnummer: String,
    val fornavn: String,
    val etternavn: String,
    val adresse: Adresse?,
)

fun PersonDTO.mapTilPerson(): Person =
    Person(
        fodselsnummer = this.socialSecurityNumber,
        fornavn = this.firstName,
        etternavn = this.lastName,
        adresse = this.address?.mapTilAdresse(),
    )

data class Adresse(
    val bruksenhetsnummer: String?,
    val adresselinje1: String?,
    val adresselinje2: String?,
    val postnummer: String,
    val poststed: String,
)

fun Address.mapTilAdresse(): Adresse =
    Adresse(
        bruksenhetsnummer = this.unitNumber,
        adresselinje1 = this.addressLine1,
        adresselinje2 = this.addressLine2,
        postnummer = this.zipCode,
        poststed = this.postalTown,
    )
