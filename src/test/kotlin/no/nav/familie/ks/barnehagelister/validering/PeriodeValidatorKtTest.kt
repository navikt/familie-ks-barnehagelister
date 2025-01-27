package no.nav.familie.ks.barnehagelister.validering

import no.nav.familie.ks.barnehagelister.domene.Barnehagebarn
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.UUID

class PeriodeValidatorKtTest {
    @Test
    fun `validerIngenOverlapp skal ikke kaste feil for ikke-overlappende perioder`() {
        val barnehagebarnList =
            listOf(
                lagBarnehagebarn(
                    fom = LocalDate.of(2021, 1, 1),
                    tom = LocalDate.of(2021, 6, 30),
                ),
                lagBarnehagebarn(
                    fom = LocalDate.of(2021, 7, 1),
                    tom = LocalDate.of(2021, 12, 31),
                ),
            )
        assertDoesNotThrow {
            barnehagebarnList.validerIngenOverlapp()
        }
    }

    @Test
    fun `validerIngenOverlapp skal kaste feil for overlappende perioder`() {
        val barnehagebarnList =
            listOf(
                lagBarnehagebarn(fom = LocalDate.of(2021, 1, 1), tom = LocalDate.of(2021, 6, 30)),
                lagBarnehagebarn(fom = LocalDate.of(2021, 6, 1), tom = LocalDate.of(2021, 12, 31)),
            )
        assertThrows<IllegalStateException> {
            barnehagebarnList.validerIngenOverlapp()
        }
    }

    @Test
    fun `validerIngenOverlapp skal ikke kaste feil for tilstøtende perioder`() {
        val barnehagebarnList =
            listOf(
                lagBarnehagebarn(fom = LocalDate.of(2021, 1, 1), tom = LocalDate.of(2021, 6, 30)),
                lagBarnehagebarn(fom = LocalDate.of(2021, 6, 30), tom = LocalDate.of(2021, 12, 31)),
            )
        assertThrows<IllegalStateException> {
            barnehagebarnList.validerIngenOverlapp()
        }
    }

    @Test
    fun `validerIngenOverlapp skal kaste feil for ikke-overlappende perioder med null tom`() {
        val barnehagebarnList =
            listOf(
                lagBarnehagebarn(fom = LocalDate.of(2021, 1, 1), tom = null),
                lagBarnehagebarn(fom = LocalDate.of(2021, 6, 1), tom = LocalDate.of(2021, 12, 31)),
            )
        assertThrows<IllegalStateException> {
            barnehagebarnList.validerIngenOverlapp()
        }
    }

    @Test
    fun `validerIngenOverlapp skal ikke kaste feil for en enkelt periode`() {
        val barnehagebarnList =
            listOf(
                lagBarnehagebarn(fom = LocalDate.of(2021, 1, 1), tom = LocalDate.of(2021, 6, 30)),
            )
        assertDoesNotThrow {
            barnehagebarnList.validerIngenOverlapp()
        }
    }

    private fun lagBarnehagebarn(
        fom: LocalDate,
        tom: LocalDate?,
    ) = Barnehagebarn(
        fom = fom,
        tom = tom,
        id = UUID.randomUUID(),
        ident = "ident",
        antallTimerIBarnehage = 40.0,
        kommuneNavn = "kommune",
        kommuneNr = "0128",
        barnehagelisteId = UUID.randomUUID(),
        organisasjonsnummer = "orgnummer",
    )
}
