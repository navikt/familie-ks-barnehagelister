package no.nav.familie.ks.barnehagelister.rest

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import jakarta.servlet.http.HttpServletRequest
import no.nav.familie.ks.barnehagelister.domene.Barnehageliste
import no.nav.familie.ks.barnehagelister.domene.BarnehagelisteValideringsfeil
import no.nav.familie.ks.barnehagelister.interceptor.hentConsumerId
import no.nav.familie.ks.barnehagelister.interceptor.hentSupplierId
import no.nav.familie.ks.barnehagelister.rest.dto.BarnehagelisteStatus
import no.nav.familie.ks.barnehagelister.rest.dto.EtterprosesseringfeilInfo
import no.nav.familie.ks.barnehagelister.rest.dto.EtterprosesseringfeilType
import no.nav.familie.ks.barnehagelister.service.BarnehagelisteMedValideringsfeil
import no.nav.familie.ks.barnehagelister.service.BarnehagelisteService
import no.nav.familie.ks.barnehagelister.testdata.FormV1RequestDtoTestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import java.time.LocalDateTime.now
import java.util.UUID

class DefaultBarnehagelisteControllerEnhetTest {
    private val mockBarnehagelisteService = mockk<BarnehagelisteService>()
    private val mockGodkjenteLeverandør = mockk<GodkjenteLeverandører>()

    private val barnehagelisteController =
        DefaultBarnehagelisteController(mockBarnehagelisteService, mockGodkjenteLeverandør)

    @Nested
    inner class StatusTest {
        @Test
        fun `Skal returnere response entity not found dersom forespurt liste ikke finnes`() {
            // Arrange
            val mocketRequest = mockk<HttpServletRequest>()
            mockkStatic(HttpServletRequest::hentConsumerId)
            mockkStatic(HttpServletRequest::hentSupplierId)

            every { mockBarnehagelisteService.hentBarnehagelisteMedValideringsfeil(any()) } returns
                BarnehagelisteMedValideringsfeil(null, emptyList())
            every { any<HttpServletRequest>().hentConsumerId() } returns "testKommune"
            every { any<HttpServletRequest>().hentSupplierId() } returns "testLeverandørOrgNr"
            every { mockGodkjenteLeverandør.leverandorer } returns
                listOf(
                    Leverandør("testLeverandørOrgNr", "testLeverandørNavn"),
                )

            // Act
            val responseEntity = barnehagelisteController.status(UUID.randomUUID(), mocketRequest)

            // Assert
            assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        }

        @Test
        fun `Skal kaste feil dersom det ikke er en godkjent leverandør som forsøker å hente status`() {
            // Arrange
            val mocketRequest = mockk<HttpServletRequest>()
            mockkStatic(HttpServletRequest::hentConsumerId)
            mockkStatic(HttpServletRequest::hentSupplierId)

            every { mockBarnehagelisteService.hentBarnehagelisteMedValideringsfeil(any()) } returns
                BarnehagelisteMedValideringsfeil(null, emptyList())
            every { any<HttpServletRequest>().hentConsumerId() } returns "testKommune"
            every { any<HttpServletRequest>().hentSupplierId() } returns "testLeverandørOrgNr2"
            every { mockGodkjenteLeverandør.leverandorer } returns
                listOf(
                    Leverandør("testLeverandørOrgNr", "testLeverandørNavn"),
                )

            // Act && Assert
            val exception =
                assertThrows<UgyldigKommuneEllerLeverandørFeil> {
                    barnehagelisteController.status(UUID.randomUUID(), mocketRequest)
                }

            assertThat(exception.message).isEqualTo("Supplier with orgno testLeverandørOrgNr2 is not a known supplier.")
        }
    }

    @Test
    fun `Skal returnere 200 OK med liste med warnings hvis det er BarnehagelisteValideringsfeil`() {
        // Arrange
        val mocketRequest = mockk<HttpServletRequest>()
        mockkStatic(HttpServletRequest::hentConsumerId)
        mockkStatic(HttpServletRequest::hentSupplierId)

        val skjema = FormV1RequestDtoTestData.lagRequest()
        val lagretBarnehageliste =
            Barnehageliste(
                id = skjema.id,
                rawJson = skjema,
                status = BarnehagelisteStatus.FERDIG,
                leverandorOrgNr = "testLeverandørOrgNr",
                kommuneOrgNr = "testKommuneOrgNr",
                ferdigTid = now(),
                opprettetTid = now(),
            )

        val lagretBarnehagelisteValideringsfeil =
            BarnehagelisteValideringsfeil(
                type = EtterprosesseringfeilType.OVERLAPPING_PERIOD_WITHIN_SAME_LIST.name,
                feilinfo = "feilinfo",
                ident = "12345678901",
                id = UUID.randomUUID(),
                barnehagelisteId = lagretBarnehageliste.id,
                opprettetTid = now(),
            )
        every { mockBarnehagelisteService.hentBarnehagelisteMedValideringsfeil(any()) } returns
            BarnehagelisteMedValideringsfeil(
                lagretBarnehageliste,
                listOf(
                    lagretBarnehagelisteValideringsfeil,
                ),
            )
        every { any<HttpServletRequest>().hentConsumerId() } returns "testKommuneOrgNr"
        every { any<HttpServletRequest>().hentSupplierId() } returns "testLeverandørOrgNr"
        every { mockGodkjenteLeverandør.leverandorer } returns
            listOf(
                Leverandør("testLeverandørOrgNr", "testLeverandørNavn"),
            )

        // Act
        val responseEntity = barnehagelisteController.status(lagretBarnehageliste.id, mocketRequest)

        // Assert
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(responseEntity.body?.status).isEqualTo(BarnehagelisteStatus.FERDIG.engelsk)
        assertThat(responseEntity.body?.finishedTime).isEqualTo(lagretBarnehageliste.ferdigTid)
        assertThat(responseEntity.body?.receivedTime).isEqualTo(lagretBarnehageliste.opprettetTid)
        assertThat(responseEntity.body?.id).isEqualTo(lagretBarnehageliste.id)
        assertThat(responseEntity.body?.links?.status).isEqualTo("/api/kindergartenlists/status/${lagretBarnehageliste.id}")
        assertThat(responseEntity.body?.links?.warnings)
            .hasSize(1)
            .contains(
                EtterprosesseringfeilInfo(
                    type = EtterprosesseringfeilType.OVERLAPPING_PERIOD_WITHIN_SAME_LIST,
                    detail = lagretBarnehagelisteValideringsfeil.feilinfo + " child=" + lagretBarnehagelisteValideringsfeil.ident,
                ),
            )
    }

    @Test
    fun `Skal kaste feil dersom det ikke er samme leverandør som forsøker å hente status på innsendt barnehageliste`() {
        // Arrange
        val mocketRequest = mockk<HttpServletRequest>()
        mockkStatic(HttpServletRequest::hentConsumerId)
        mockkStatic(HttpServletRequest::hentSupplierId)

        val lagretBarnehageliste =
            Barnehageliste(
                id = UUID.randomUUID(),
                rawJson = FormV1RequestDtoTestData.lagRequest(),
                status = BarnehagelisteStatus.FERDIG,
                leverandorOrgNr = "testLeverandørOrgNr3",
                kommuneOrgNr = "testKommuneOrgNr",
            )

        every { mockBarnehagelisteService.hentBarnehagelisteMedValideringsfeil(any()) } returns
            BarnehagelisteMedValideringsfeil(lagretBarnehageliste, emptyList())
        every { any<HttpServletRequest>().hentConsumerId() } returns "testKommuneOrgNr"
        every { any<HttpServletRequest>().hentSupplierId() } returns "testLeverandørOrgNr2"
        every { mockGodkjenteLeverandør.leverandorer } returns
            listOf(
                Leverandør("testLeverandørOrgNr2", "testLeverandørNavn"),
            )

        // Act && Assert
        val exception =
            assertThrows<UgyldigKommuneEllerLeverandørFeil> {
                barnehagelisteController.status(UUID.randomUUID(), mocketRequest)
            }

        assertThat(exception.message).isEqualTo("The requested kindergarten list were not sent in by supplier testLeverandørOrgNr2")
    }

    @Test
    fun `Skal kaste feil dersom det ikke er samme kommune som forsøker å hente status på innsendt barnehageliste`() {
        // Arrange
        val mocketRequest = mockk<HttpServletRequest>()
        mockkStatic(HttpServletRequest::hentConsumerId)
        mockkStatic(HttpServletRequest::hentSupplierId)

        val lagretBarnehageliste =
            Barnehageliste(
                id = UUID.randomUUID(),
                rawJson = FormV1RequestDtoTestData.lagRequest(),
                status = BarnehagelisteStatus.FERDIG,
                leverandorOrgNr = "testLeverandørOrgNr3",
                kommuneOrgNr = "testKommuneOrgNr",
            )

        every { mockBarnehagelisteService.hentBarnehagelisteMedValideringsfeil(any()) } returns
            BarnehagelisteMedValideringsfeil(lagretBarnehageliste, emptyList())
        every { any<HttpServletRequest>().hentConsumerId() } returns "12345"
        every { any<HttpServletRequest>().hentSupplierId() } returns "testLeverandørOrgNr3"
        every { mockGodkjenteLeverandør.leverandorer } returns
            listOf(
                Leverandør("testLeverandørOrgNr3", "testLeverandørNavn"),
            )

        // Act & Assert
        val exception =
            assertThrows<UgyldigKommuneEllerLeverandørFeil> {
                barnehagelisteController.status(UUID.randomUUID(), mocketRequest)
            }

        assertThat(exception.message).isEqualTo("The requested kindergarten list were not sent in by municipality with org id 12345")
    }

    @Test
    fun `Skal returnere status dersom barnehageliste forsøkes hent av samme kommune og leverandør`() {
        // Arrange
        val mocketRequest = mockk<HttpServletRequest>()
        mockkStatic(HttpServletRequest::hentConsumerId)
        mockkStatic(HttpServletRequest::hentSupplierId)

        val uuid = UUID.randomUUID()
        val lagretBarnehageliste =
            Barnehageliste(
                id = uuid,
                rawJson = FormV1RequestDtoTestData.lagRequest(),
                status = BarnehagelisteStatus.FERDIG,
                leverandorOrgNr = "testLeverandørOrgNr3",
                kommuneOrgNr = "testKommuneOrgNr",
            )

        every { mockBarnehagelisteService.hentBarnehagelisteMedValideringsfeil(any()) } returns
            BarnehagelisteMedValideringsfeil(lagretBarnehageliste, emptyList())
        every { any<HttpServletRequest>().hentConsumerId() } returns "testKommuneOrgNr"
        every { any<HttpServletRequest>().hentSupplierId() } returns "testLeverandørOrgNr3"
        every { mockGodkjenteLeverandør.leverandorer } returns
            listOf(
                Leverandør("testLeverandørOrgNr3", "testLeverandørNavn"),
            )

        // Act
        val responseEntity = barnehagelisteController.status(uuid, mocketRequest)

        // Assert
        assertThat(responseEntity.body?.status).isEqualTo(BarnehagelisteStatus.FERDIG.engelsk)
    }

    @Test
    fun `Skal returnere valideringfeil hvis det finnes valideringsfeil i databasen`() {
        // Arrange
        val mocketRequest = mockk<HttpServletRequest>()
        mockkStatic(HttpServletRequest::hentConsumerId)
        mockkStatic(HttpServletRequest::hentSupplierId)

        every { mockBarnehagelisteService.hentBarnehagelisteMedValideringsfeil(any()) } returns
            BarnehagelisteMedValideringsfeil(null, emptyList())
        every { any<HttpServletRequest>().hentConsumerId() } returns "testKommune"
        every { any<HttpServletRequest>().hentSupplierId() } returns "testLeverandørOrgNr"
        every { mockGodkjenteLeverandør.leverandorer } returns
            listOf(
                Leverandør("testLeverandørOrgNr", "testLeverandørNavn"),
            )

        // Act
        val responseEntity = barnehagelisteController.status(UUID.randomUUID(), mocketRequest)

        // Assert
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }
}
