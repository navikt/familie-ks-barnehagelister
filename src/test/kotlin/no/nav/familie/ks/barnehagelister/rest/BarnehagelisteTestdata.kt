package no.nav.familie.ks.barnehagelister.rest

import no.nav.familie.ks.barnehagelister.kontrakt.*
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class BarnehagelisteTestdata {
    companion object {
        fun gyldigBarnehageliste(): FormV1 =
            FormV1(
                id = UUID.randomUUID(),
                listInformation =
                    ListInformation(
                        municipalityName = "Oslo",
                        municipalityNumber = "0301",
                        submissionForYearMonth = YearMonth.now(),
                    ),
                kindergartens =
                    listOf(
                        lagBarnehage(),
                    ),
            )

        fun lagBarnehage() =
            Kindergarten(
                organizationNumber = "123456789",
                name = "Eksempel Barnehage",
                address =
                    Address(
                        unitNumber = "H0101",
                        addressLine1 = "Svingen 1",
                        addressLine2 = null,
                        zipCode = "0102",
                        postalTown = "Oslo",
                    ),
                childrenInformation =
                    listOf(
                        lagBarninfolinje(),
                    ),
            )

        fun lagBarninfolinje() =
            ChildInformation(
                agreedHoursInKindergarten = 37.5,
                startDate = LocalDate.of(2023, 1, 1),
                endDate = null,
                child =
                    PersonDTO(
                        socialSecurityNumber = "12345678910",
                        firstName = "Ola",
                        lastName = "Nordmann",
                        address =
                            Address(
                                unitNumber = "H0101",
                                addressLine1 = "Svingen 1",
                                addressLine2 = null,
                                zipCode = "0102",
                                postalTown = "Oslo",
                            ),
                    ),
                guardians =
                    listOf(
                        PersonDTO(
                            socialSecurityNumber = "10987654321",
                            firstName = "Kari",
                            lastName = "Nordmann",
                            address =
                                Address(
                                    unitNumber = "H0101",
                                    addressLine1 = "Svingen 1",
                                    addressLine2 = null,
                                    zipCode = "0102",
                                    postalTown = "Oslo",
                                ),
                        ),
                    ),
            )

        fun barnehagelistBlankeFelt(): FormV1 =
            FormV1(
                id = UUID.randomUUID(),
                listInformation =
                    ListInformation(
                        municipalityNumber = " ",
                        municipalityName = " ",
                        submissionForYearMonth = YearMonth.now(),
                    ),
                kindergartens =
                    listOf(
                        Kindergarten(
                            organizationNumber = " ",
                            name = " ",
                            address =
                                Address(
                                    unitNumber = "",
                                    addressLine1 = "",
                                    addressLine2 = "",
                                    zipCode = "",
                                    postalTown = "",
                                ),
                            childrenInformation =
                                listOf(
                                    ChildInformation(
                                        agreedHoursInKindergarten = 37.5,
                                        startDate = LocalDate.of(2023, 1, 1),
                                        endDate = null,
                                        child =
                                            PersonDTO(
                                                socialSecurityNumber = "12345678910",
                                                firstName = "Ola",
                                                lastName = "Nordmann",
                                                address =
                                                    Address(
                                                        unitNumber = "H0101",
                                                        addressLine1 = "Svingen 1",
                                                        addressLine2 = null,
                                                        zipCode = "0102",
                                                        postalTown = "Oslo",
                                                    ),
                                            ),
                                        guardians =
                                            listOf(
                                                PersonDTO(
                                                    socialSecurityNumber = "",
                                                    firstName = "",
                                                    lastName = "",
                                                    address =
                                                        Address(
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
