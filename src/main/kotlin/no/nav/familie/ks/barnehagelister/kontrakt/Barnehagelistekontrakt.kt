package no.nav.familie.ks.barnehagelister.kontrakt

import java.time.LocalDate
import java.util.UUID

data class Skjema(
    var id: UUID,
    var barnInfolinjer: List<BarnInfolinje>,
    var listeopplysninger: Listeopplysninger,
)

data class Listeopplysninger(
    var kommuneNavn: String,
    var kommuneNr: String,
    var aarInnsending: String,
    var maanedinnsending: String,
)

data class BarnInfolinje(
    var avtaltOppholdstidTimer: Double,
    var startdato: LocalDate,
    var sluttdato: LocalDate?,
    var barn: Barn,
    var foreldre: List<Forelder>,
    var barnehage: Barnehage,
    var endringstype: String,
)

data class Barnehage(
    var navn: String,
    var organisasjonsnr: String,
    var adresse: Adresse?,
)

data class Forelder(
    var fodselsnummer: String,
    var navn: String,
    var adresse: Adresse?,
)

data class Barn(
    var navn: String,
    var fodselsnummer: String,
    var adresse: Adresse?,
)

data class Adresse(
    var adresselinje1: String?,
    var adresselinje2: String?,
    var postnummer: String,
    var poststed: String,
)
