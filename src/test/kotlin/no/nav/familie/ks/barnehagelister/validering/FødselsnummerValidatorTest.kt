package no.nav.familie.ks.barnehagelister.validering

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FødselsnummerValidatorTest {
    val fødselsnummerValidator = FødselsnummerValidator()

    @Test
    fun `Gyldig fødselsnummer skal returnere true`() {
        assertTrue(fødselsnummerValidator.isValid("30438227985", null))
    }

    @Test
    fun `Ugyldig fødselsnummer skal returnere false`() {
        assertFalse(fødselsnummerValidator.isValid("30438227986", null))
    }
}
