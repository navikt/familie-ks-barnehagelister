package no.nav.familie.ks.barnehagelister.rest

import no.nav.familie.ks.barnehagelister.kontrakt.Adresse
import no.nav.familie.ks.barnehagelister.kontrakt.BarnInfolinje
import no.nav.familie.ks.barnehagelister.kontrakt.Barnehage
import no.nav.familie.ks.barnehagelister.kontrakt.Listeopplysninger
import no.nav.familie.ks.barnehagelister.kontrakt.Person
import no.nav.familie.ks.barnehagelister.kontrakt.SkjemaV1
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class BarnehagelisteTestdata {
    companion object {
        fun gyldigBarnehageliste(): SkjemaV1 =
            SkjemaV1(
                id = UUID.randomUUID(),
                listeopplysninger =
                    Listeopplysninger(
                        kommunenummer = "0301",
                        kommunenavn = "Oslo",
                        innsendingGjelderArManed = YearMonth.now(),
                    ),
                barnehager =
                    listOf(
                        lagBarnehage(),
                    ),
            )

        fun lagBarnehage() =
            Barnehage(
                organisasjonsnummer = "123456789",
                navn = "Eksempel Barnehage",
                adresse =
                    Adresse(
                        bruksenhetsnummer = "H0101",
                        adresselinje1 = "Svingen 1",
                        adresselinje2 = null,
                        postnummer = "0102",
                        poststed = "Oslo",
                    ),
                barnInfolinjer =
                    listOf(
                        lagBarninfolinje(),
                    ),
            )

        fun lagBarninfolinje() =
            BarnInfolinje(
                avtaltOppholdstidTimer = 37.5,
                startdato = LocalDate.of(2023, 1, 1),
                sluttdato = null,
                barn =
                    Person(
                        fodselsnummer = "12345678910",
                        fornavn = "Ola",
                        etternavn = "Nordmann",
                        adresse =
                            Adresse(
                                bruksenhetsnummer = "H0101",
                                adresselinje1 = "Svingen 1",
                                adresselinje2 = null,
                                postnummer = "0102",
                                poststed = "Oslo",
                            ),
                    ),
                foresatte =
                    listOf(
                        Person(
                            fodselsnummer = "10987654321",
                            fornavn = "Kari",
                            etternavn = "Nordmann",
                            adresse =
                                Adresse(
                                    bruksenhetsnummer = "H0101",
                                    adresselinje1 = "Svingen 1",
                                    adresselinje2 = null,
                                    postnummer = "0102",
                                    poststed = "Oslo",
                                ),
                        ),
                    ),
            )

        fun barnehagelistBlankeFelt(): SkjemaV1 =
            SkjemaV1(
                id = UUID.randomUUID(),
                listeopplysninger =
                    Listeopplysninger(
                        kommunenummer = " ",
                        kommunenavn = " ",
                        innsendingGjelderArManed = YearMonth.now(),
                    ),
                barnehager =
                    listOf(
                        Barnehage(
                            organisasjonsnummer = " ",
                            navn = " ",
                            adresse =
                                Adresse(
                                    bruksenhetsnummer = "",
                                    adresselinje1 = "",
                                    adresselinje2 = "",
                                    postnummer = "",
                                    poststed = "",
                                ),
                            barnInfolinjer =
                                listOf(
                                    BarnInfolinje(
                                        avtaltOppholdstidTimer = 37.5,
                                        startdato = LocalDate.of(2023, 1, 1),
                                        sluttdato = null,
                                        barn =
                                            Person(
                                                fodselsnummer = "12345678910",
                                                fornavn = "Ola",
                                                etternavn = "Nordmann",
                                                adresse =
                                                    Adresse(
                                                        bruksenhetsnummer = "H0101",
                                                        adresselinje1 = "Svingen 1",
                                                        adresselinje2 = null,
                                                        postnummer = "0102",
                                                        poststed = "Oslo",
                                                    ),
                                            ),
                                        foresatte =
                                            listOf(
                                                Person(
                                                    fodselsnummer = "",
                                                    fornavn = "",
                                                    etternavn = "",
                                                    adresse =
                                                        Adresse(
                                                            bruksenhetsnummer = "",
                                                            adresselinje1 = "Svingen 1",
                                                            adresselinje2 = null,
                                                            postnummer = "0102",
                                                            poststed = "Oslo",
                                                        ),
                                                ),
                                            ),
                                    ),
                                ),
                        ),
                    ),
            )
    }
}
