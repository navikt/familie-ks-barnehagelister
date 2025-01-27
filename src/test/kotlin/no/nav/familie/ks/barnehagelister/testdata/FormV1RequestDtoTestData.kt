package no.nav.familie.ks.barnehagelister.testdata

import no.nav.familie.ks.barnehagelister.domene.Barnehagebarn
import no.nav.familie.ks.barnehagelister.rest.dto.ChildInformationRequestDto
import no.nav.familie.ks.barnehagelister.rest.dto.FormV1RequestDto
import no.nav.familie.ks.barnehagelister.rest.dto.KindergartenRequestDto
import no.nav.familie.ks.barnehagelister.rest.dto.ListInformationRequestDto
import no.nav.familie.ks.barnehagelister.rest.dto.PersonRequestDto
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class FormV1RequestDtoTestData {
    companion object {
        private val id = UUID.randomUUID()

        fun lagRequest() =
            FormV1RequestDto(
                id = id,
                kindergartens = listOf(lagBarnehage()),
                listInformation = lagListeOpplysninger(),
            )

        fun lagListeOpplysninger() =
            ListInformationRequestDto(
                municipalityName = "Kommune 1",
                municipalityNumber = "1234",
                submissionForYearMonth = YearMonth.now(),
            )

        fun lagBarnehage() =
            KindergartenRequestDto(
                name = "Gullklumpen Barnehage AS",
                organizationNumber = "310028142",
                address = null,
                childrenInformation =
                    listOf(
                        lagBarnInfolinje(),
                    ),
            )

        fun lagBarnInfolinje() =
            ChildInformationRequestDto(
                child = lagBarn(),
                agreedHoursInKindergarten = 40.0,
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusMonths(5),
                guardians = null,
            )

        fun lagBarn() =
            PersonRequestDto(
                socialSecurityNumber = "12345678901",
                firstName = "navn",
                lastName = "navnesen",
                address = null,
            )

        fun lagTilhørendeBarnehagebarn(barnehagelisteId: UUID = id) =
            Barnehagebarn(
                ident = lagBarn().socialSecurityNumber,
                fom = lagBarnInfolinje().startDate,
                tom = lagBarnInfolinje().endDate,
                antallTimerIBarnehage = lagBarnInfolinje().agreedHoursInKindergarten,
                kommuneNavn = lagListeOpplysninger().municipalityName,
                kommuneNr = lagListeOpplysninger().municipalityNumber,
                barnehagelisteId = barnehagelisteId,
                organisasjonsnummer = lagBarnehage().organizationNumber,
            )
    }
}
