package no.nav.familie.ks.barnehagelister.rest

import no.nav.familie.ks.barnehagelister.rest.dto.AddressRequestDto
import no.nav.familie.ks.barnehagelister.rest.dto.ChildInformationRequestDto
import no.nav.familie.ks.barnehagelister.rest.dto.FormV1RequestDto
import no.nav.familie.ks.barnehagelister.rest.dto.KindergartenRequestDto
import no.nav.familie.ks.barnehagelister.rest.dto.ListInformationRequestDto
import no.nav.familie.ks.barnehagelister.rest.dto.PersonRequestDto
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class BarnehagelisteTestdata {
    companion object {
        fun gyldigBarnehageliste(): FormV1RequestDto =
            FormV1RequestDto(
                id = UUID.randomUUID(),
                listInformation =
                    ListInformationRequestDto(
                        municipalityName = "Oslo",
                        municipalityNumber = "0301",
                        submissionForYearMonth = YearMonth.now(),
                    ),
                kindergartens =
                    listOf(
                        lagKindergartenRequestDto(),
                    ),
            )

        fun lagKindergartenRequestDto() =
            KindergartenRequestDto(
                organizationNumber = "123456789",
                name = "Eksempel Barnehage",
                address =
                    AddressRequestDto(
                        unitNumber = "H0101",
                        addressLine1 = "Svingen 1",
                        addressLine2 = null,
                        zipCode = "0102",
                        postalTown = "Oslo",
                    ),
                childrenInformation =
                    listOf(
                        lagChildInformationRequestDto(),
                    ),
            )

        fun lagChildInformationRequestDto() =
            ChildInformationRequestDto(
                agreedHoursInKindergarten = 37.5,
                startDate = LocalDate.of(2023, 1, 1),
                endDate = null,
                child =
                    PersonRequestDto(
                        socialSecurityNumber = "12345678910",
                        firstName = "Ola",
                        lastName = "Nordmann",
                        address =
                            AddressRequestDto(
                                unitNumber = "H0101",
                                addressLine1 = "Svingen 1",
                                addressLine2 = null,
                                zipCode = "0102",
                                postalTown = "Oslo",
                            ),
                    ),
                guardians =
                    listOf(
                        PersonRequestDto(
                            socialSecurityNumber = "10987654321",
                            firstName = "Kari",
                            lastName = "Nordmann",
                            address =
                                AddressRequestDto(
                                    unitNumber = "H0101",
                                    addressLine1 = "Svingen 1",
                                    addressLine2 = null,
                                    zipCode = "0102",
                                    postalTown = "Oslo",
                                ),
                        ),
                    ),
            )

        fun barnehagelistBlankeFelt(): FormV1RequestDto =
            FormV1RequestDto(
                id = UUID.randomUUID(),
                listInformation =
                    ListInformationRequestDto(
                        municipalityNumber = " ",
                        municipalityName = " ",
                        submissionForYearMonth = YearMonth.now(),
                    ),
                kindergartens =
                    listOf(
                        KindergartenRequestDto(
                            organizationNumber = " ",
                            name = " ",
                            address =
                                AddressRequestDto(
                                    unitNumber = "",
                                    addressLine1 = "",
                                    addressLine2 = "",
                                    zipCode = "",
                                    postalTown = "",
                                ),
                            childrenInformation =
                                listOf(
                                    ChildInformationRequestDto(
                                        agreedHoursInKindergarten = 37.5,
                                        startDate = LocalDate.of(2023, 1, 1),
                                        endDate = null,
                                        child =
                                            PersonRequestDto(
                                                socialSecurityNumber = "12345678910",
                                                firstName = "Ola",
                                                lastName = "Nordmann",
                                                address =
                                                    AddressRequestDto(
                                                        unitNumber = "H0101",
                                                        addressLine1 = "Svingen 1",
                                                        addressLine2 = null,
                                                        zipCode = "0102",
                                                        postalTown = "Oslo",
                                                    ),
                                            ),
                                        guardians =
                                            listOf(
                                                PersonRequestDto(
                                                    socialSecurityNumber = "",
                                                    firstName = "",
                                                    lastName = "",
                                                    address =
                                                        AddressRequestDto(
                                                            unitNumber = "",
                                                            addressLine1 = "Svingen 1",
                                                            addressLine2 = null,
                                                            zipCode = "0102",
                                                            postalTown = "Oslo",
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
