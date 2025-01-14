package no.nav.familie.ks.barnehagelister.testdata

import no.nav.familie.ks.barnehagelister.domene.BarnInfolinje
import no.nav.familie.ks.barnehagelister.domene.Barnehage
import no.nav.familie.ks.barnehagelister.domene.Listeopplysninger
import no.nav.familie.ks.barnehagelister.domene.Person
import no.nav.familie.ks.barnehagelister.domene.SkjemaV1
import no.nav.familie.ks.barnehagelister.kafka.Barnehagebarn
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class SkjemaV1TestData {
    companion object {
        private val skjemaV1Id = UUID.randomUUID()

        fun lagSkjemaV1() =
            SkjemaV1(
                id = skjemaV1Id,
                barnehager = listOf(lagBarnehage()),
                listeopplysninger = lagListeOpplysninger(),
            )

        fun lagListeOpplysninger() =
            Listeopplysninger(
                kommunenavn = "Kommune 1",
                kommunenummer = "1234",
                innsendingGjelderArManed = YearMonth.now(),
            )

        fun lagBarnehage() =
            Barnehage(
                navn = "Gullklumpen Barnehage AS",
                organisasjonsnummer = "310028142",
                adresse = null,
                barnInfolinjer =
                    listOf(
                        lagBarnInfolinje(),
                    ),
            )

        fun lagBarnInfolinje() =
            BarnInfolinje(
                barn = lagBarn(),
                avtaltOppholdstidTimer = 40.0,
                startdato = LocalDate.now(),
                sluttdato = LocalDate.now().plusMonths(5),
                foresatte = null,
            )

        fun lagBarn() =
            Person(
                fodselsnummer = "12345678901",
                fornavn = "navn",
                etternavn = "navnesen",
                adresse = null,
            )

        // BarnehageBarnKs som samsvarer med lagSkjemaV1()
        fun lagTilhørendeBarnehageBarnKs(barnehagelisteId: UUID = skjemaV1Id) =
            Barnehagebarn(
                ident = lagBarn().fodselsnummer,
                fom = lagBarnInfolinje().startdato,
                tom = lagBarnInfolinje().sluttdato,
                antallTimerIBarnehage = lagBarnInfolinje().avtaltOppholdstidTimer,
                kommuneNavn = lagListeOpplysninger().kommunenavn,
                kommuneNr = lagListeOpplysninger().kommunenummer,
                barnehagelisteId = barnehagelisteId,
                organisasjonsnummer = lagBarnehage().organisasjonsnummer,
            )
    }
}
