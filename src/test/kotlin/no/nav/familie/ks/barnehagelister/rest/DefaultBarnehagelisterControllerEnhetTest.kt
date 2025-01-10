package no.nav.familie.ks.barnehagelister.rest

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import jakarta.servlet.http.HttpServletRequest
import no.nav.familie.ks.barnehagelister.domene.BarnehagelisteService
import no.nav.familie.ks.barnehagelister.domene.Barnehagelister
import no.nav.familie.ks.barnehagelister.interceptor.hentConsumerId
import no.nav.familie.ks.barnehagelister.interceptor.hentSupplierId
import no.nav.familie.ks.barnehagelister.rest.dto.BarnehagelisteStatus
import no.nav.familie.ks.barnehagelister.testdata.SkjemaV1TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import java.util.UUID

class DefaultBarnehagelisterControllerEnhetTest {
    private val mockBarnehagelisteService = mockk<BarnehagelisteService>()
    private val mockGodkjenteLeverandør = mockk<GodkjenteLeverandører>()

    private val barnehagelisterController =
        DefaultBarnehagelisterController(mockBarnehagelisteService, mockGodkjenteLeverandør)

    @Nested
    inner class StatusTest {
        @Test
        fun `Skal returnere response entity not found dersom forespurt liste ikke finnes`() {
            // Arrange
            val mocketRequest = mockk<HttpServletRequest>()
            mockkStatic(HttpServletRequest::hentConsumerId)
            mockkStatic(HttpServletRequest::hentSupplierId)

            every { mockBarnehagelisteService.hentBarnehagelister(any()) } returns null
            every { any<HttpServletRequest>().hentConsumerId() } returns "testKommune"
            every { any<HttpServletRequest>().hentSupplierId() } returns "testLeverandørOrgNr"
            every { mockGodkjenteLeverandør.leverandorer } returns
                listOf(
                    Leverandør("testLeverandørOrgNr", "testLeverandørNavn"),
                )

            // Act
            val responseEntity = barnehagelisterController.status(UUID.randomUUID(), mocketRequest)

            // Assert
            assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        }

        @Test
        fun `Skal kaste feil dersom det ikke er en godkjent leverandør som forsøker å hente status`() {
            // Arrange
            val mocketRequest = mockk<HttpServletRequest>()
            mockkStatic(HttpServletRequest::hentConsumerId)
            mockkStatic(HttpServletRequest::hentSupplierId)

            every { mockBarnehagelisteService.hentBarnehagelister(any()) } returns null
            every { any<HttpServletRequest>().hentConsumerId() } returns "testKommune"
            every { any<HttpServletRequest>().hentSupplierId() } returns "testLeverandørOrgNr2"
            every { mockGodkjenteLeverandør.leverandorer } returns
                listOf(
                    Leverandør("testLeverandørOrgNr", "testLeverandørNavn"),
                )

            // Act && Assert
            val exception =
                assertThrows<UgyldigKommuneEllerLeverandørFeil> {
                    barnehagelisterController.status(UUID.randomUUID(), mocketRequest)
                }

            assertThat(exception.message).isEqualTo("Supplier with orgno testLeverandørOrgNr2 is not a known supplier.")
        }
    }

    @Test
    fun `Skal kaste feil dersom det ikke er samme leverandør som forsøker å hente status på innsendt barnehagelister`() {
        // Arrange
        val mocketRequest = mockk<HttpServletRequest>()
        mockkStatic(HttpServletRequest::hentConsumerId)
        mockkStatic(HttpServletRequest::hentSupplierId)

        val lagetBarnehagelister =
            Barnehagelister(
                id = UUID.randomUUID(),
                rawJson = SkjemaV1TestData.lagSkjemaV1(),
                status = BarnehagelisteStatus.FERDIG,
                leverandorOrgNr = "testLeverandørOrgNr3",
            )

        every { mockBarnehagelisteService.hentBarnehagelister(any()) } returns lagetBarnehagelister
        every { any<HttpServletRequest>().hentConsumerId() } returns "1234"
        every { any<HttpServletRequest>().hentSupplierId() } returns "testLeverandørOrgNr2"
        every { mockGodkjenteLeverandør.leverandorer } returns
            listOf(
                Leverandør("testLeverandørOrgNr2", "testLeverandørNavn"),
            )

        // Act && Assert
        val exception =
            assertThrows<UgyldigKommuneEllerLeverandørFeil> {
                barnehagelisterController.status(UUID.randomUUID(), mocketRequest)
            }

        assertThat(exception.message).isEqualTo("The requested kindergarten list were not sent in by supplier testLeverandørOrgNr2")
    }

    @Test
    fun `Skal kaste feil dersom det ikke er samme kommune som forsøker å hente status på innsendt barnehagelister`() {
        // Arrange
        val mocketRequest = mockk<HttpServletRequest>()
        mockkStatic(HttpServletRequest::hentConsumerId)
        mockkStatic(HttpServletRequest::hentSupplierId)

        val lagetBarnehagelister =
            Barnehagelister(
                id = UUID.randomUUID(),
                rawJson = SkjemaV1TestData.lagSkjemaV1(),
                status = BarnehagelisteStatus.FERDIG,
                leverandorOrgNr = "testLeverandørOrgNr3",
            )

        every { mockBarnehagelisteService.hentBarnehagelister(any()) } returns lagetBarnehagelister
        every { any<HttpServletRequest>().hentConsumerId() } returns "12345"
        every { any<HttpServletRequest>().hentSupplierId() } returns "testLeverandørOrgNr3"
        every { mockGodkjenteLeverandør.leverandorer } returns
            listOf(
                Leverandør("testLeverandørOrgNr3", "testLeverandørNavn"),
            )

        // Act & Assert
        val exception =
            assertThrows<UgyldigKommuneEllerLeverandørFeil> {
                barnehagelisterController.status(UUID.randomUUID(), mocketRequest)
            }

        assertThat(exception.message).isEqualTo("The requested kindergarten list were not sent in by municipality 12345")
    }

    @Test
    fun `Skal returnere status dersom barnehagelister forsøkes hent av samme kommune og leverandør`() {
        // Arrange
        val mocketRequest = mockk<HttpServletRequest>()
        mockkStatic(HttpServletRequest::hentConsumerId)
        mockkStatic(HttpServletRequest::hentSupplierId)

        val uuid = UUID.randomUUID()
        val lagetBarnehagelister =
            Barnehagelister(
                id = uuid,
                rawJson = SkjemaV1TestData.lagSkjemaV1(),
                status = BarnehagelisteStatus.FERDIG,
                leverandorOrgNr = "testLeverandørOrgNr3",
            )

        every { mockBarnehagelisteService.hentBarnehagelister(any()) } returns lagetBarnehagelister
        every { any<HttpServletRequest>().hentConsumerId() } returns "1234"
        every { any<HttpServletRequest>().hentSupplierId() } returns "testLeverandørOrgNr3"
        every { mockGodkjenteLeverandør.leverandorer } returns
            listOf(
                Leverandør("testLeverandørOrgNr3", "testLeverandørNavn"),
            )

        // Act
        val responseEntity = barnehagelisterController.status(uuid, mocketRequest)

        // Assert
        assertThat(responseEntity.body?.status).isEqualTo(BarnehagelisteStatus.MOTTATT.engelsk)
    }
}
