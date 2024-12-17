package no.nav.familie.ks.barnehagelister.domene

import no.nav.familie.ks.barnehagelister.rest.dto.AddressRequestDto
import no.nav.familie.ks.barnehagelister.rest.dto.ChildInformationRequestDto
import no.nav.familie.ks.barnehagelister.rest.dto.FormV1RequestDto
import no.nav.familie.ks.barnehagelister.rest.dto.KindergartenRequestDto
import no.nav.familie.ks.barnehagelister.rest.dto.ListInformationRequestDto
import no.nav.familie.ks.barnehagelister.rest.dto.PersonRequestDto
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

// TODO sanitize JSON input according to OWASP
data class SkjemaV1(
    val id: UUID,
    val barnehager: List<Barnehage>?,
    val listeopplysninger: Listeopplysninger,
)

fun FormV1RequestDto.mapTilSkjemaV1(): SkjemaV1 =
    SkjemaV1(
        id = this.id,
        barnehager = this.kindergartens?.map { it.mapTilBarnehage() },
        listeopplysninger = this.listInformationRequestDto.mapTilListeopplysninger(),
    )

data class Listeopplysninger(
    val kommunenavn: String,
    val kommunenummer: String,
    val innsendingGjelderArManed: YearMonth,
)

fun ListInformationRequestDto.mapTilListeopplysninger(): Listeopplysninger =
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

fun KindergartenRequestDto.mapTilBarnehage(): Barnehage =
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

fun ChildInformationRequestDto.mapTilBarnInfolinje(): BarnInfolinje =
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

fun PersonRequestDto.mapTilPerson(): Person =
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

fun AddressRequestDto.mapTilAdresse(): Adresse =
    Adresse(
        bruksenhetsnummer = this.unitNumber,
        adresselinje1 = this.addressLine1,
        adresselinje2 = this.addressLine2,
        postnummer = this.zipCode,
        poststed = this.postalTown,
    )
