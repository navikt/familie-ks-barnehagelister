package no.nav.familie.ks.barnehagelister.validering

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class OrganisasjonsnummerValidatorTest {
    val organisasjonsnummerValidator = OrganisasjonsnummerValidator()

    @ParameterizedTest(name = "={0}")
    @ValueSource(strings = ["889640782", "310028142"])
    fun `Gyldige organisasjonsnummer for både prod og test skal returnere true`(organisasjonsnummer: String) {
        assertTrue(organisasjonsnummerValidator.isValid(organisasjonsnummer, null))
    }

    @Test
    fun `Ugyldig organisasjonsnummer pga lengde skal returnere false`() {
        assertFalse(organisasjonsnummerValidator.isValid("12345678", null))
    }

    @Test
    fun `Ugyldig organisasjonsnummer skal returnere false`() {
        assertFalse(organisasjonsnummerValidator.isValid("123456789", null))
    }
}
