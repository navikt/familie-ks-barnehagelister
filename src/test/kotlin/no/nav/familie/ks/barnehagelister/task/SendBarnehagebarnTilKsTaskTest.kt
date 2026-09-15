package no.nav.familie.ks.barnehagelister.task

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.familie.ks.barnehagelister.domene.Barnehageliste
import no.nav.familie.ks.barnehagelister.kafka.DummyBarnehagebarnKafkaProducer
import no.nav.familie.ks.barnehagelister.metrics.BarnehagebarnMetrikker
import no.nav.familie.ks.barnehagelister.repository.BarnehagebarnRepository
import no.nav.familie.ks.barnehagelister.rest.dto.BarnehagelisteStatus
import no.nav.familie.ks.barnehagelister.service.BarnehagelisteService
import no.nav.familie.ks.barnehagelister.testdata.FormV1RequestDtoTestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class SendBarnehagebarnTilKsTaskTest {
    val barnehagelisteService = mockk<BarnehagelisteService>()
    val barnehagebarnKafkaProducer = mockk<DummyBarnehagebarnKafkaProducer>()
    val barnehagebarnRepository = mockk<BarnehagebarnRepository>()

    val meterRegistry = SimpleMeterRegistry()
    val barnehagebarnMetrikker = BarnehagebarnMetrikker(meterRegistry)

    val sendBarnehagebarnTilKsTask =
        SendBarnehagebarnTilKsTask(
            barnehagebarnRepository = barnehagebarnRepository,
            barnehagelisteService = barnehagelisteService,
            barnehagebarnKafkaProducer = barnehagebarnKafkaProducer,
            barnehagebarnMetrikker = barnehagebarnMetrikker,
        )

    val barnehagelisteId = UUID.randomUUID()

    @BeforeEach()
    fun mockkReturns() {
        clearAllMocks()
        every { barnehagebarnKafkaProducer.sendBarnehageBarn(any()) } just runs
    }

    @Test
    fun `Skal legge barnehagebarn på kø`() {
        // Arrange
        val barnehageliste =
            Barnehageliste(
                id = barnehagelisteId,
                rawJson = FormV1RequestDtoTestData.lagRequest(),
                status = BarnehagelisteStatus.FERDIG,
                leverandorOrgNr = "123456789",
                kommuneOrgNr = "123456789",
            )

        val barnehagebarn =
            FormV1RequestDtoTestData.lagTilhørendeBarnehagebarn(barnehagelisteId)

        every { barnehagebarnRepository.findByIdOrNull(barnehagebarn.id) } returns barnehagebarn
        every { barnehagelisteService.hentBarnehageliste(barnehagelisteId) } returns barnehageliste

        val sendBarnehagelisteTask =
            SendBarnehagebarnTilKsTask.opprettTask(
                barnehagebarnId = barnehagebarn.id.toString(),
                barnehagelisteId = barnehagelisteId.toString(),
            )

        // Act
        sendBarnehagebarnTilKsTask.doTask(sendBarnehagelisteTask)

        // Assert
        verify(exactly = 1) { barnehagebarnKafkaProducer.sendBarnehageBarn(barnehagebarn) }
        assertThat(
            meterRegistry
                .counter(BarnehagebarnMetrikker.BARN_SENDT_TIL_KS, "kommune_nr", barnehagebarn.kommuneNr)
                .count(),
        ).isEqualTo(1.0)
    }

    @Test
    fun `Skal kaste feil hvis barnehagelisten ikke er ferdig`() {
        // Arrange
        val barnehageliste =
            Barnehageliste(
                id = barnehagelisteId,
                rawJson = FormV1RequestDtoTestData.lagRequest(),
                status = BarnehagelisteStatus.MOTTATT,
                leverandorOrgNr = "123456789",
                kommuneOrgNr = "123456789",
            )

        val barnehagebarn =
            FormV1RequestDtoTestData.lagTilhørendeBarnehagebarn(barnehagelisteId)

        every { barnehagebarnRepository.findByIdOrNull(barnehagebarn.id) } returns barnehagebarn
        every { barnehagelisteService.hentBarnehageliste(barnehagelisteId) } returns barnehageliste

        val sendBarnehagelisteTask =
            SendBarnehagebarnTilKsTask.opprettTask(
                barnehagebarnId = barnehagebarn.id.toString(),
                barnehagelisteId = barnehagelisteId.toString(),
            )

        // Act && Assert
        val exception =
            assertThrows<IllegalStateException> {
                sendBarnehagebarnTilKsTask.doTask(sendBarnehagelisteTask)
            }

        // Assert
        assertThat(exception.message).isEqualTo("Barnehageliste med id $barnehagelisteId er ikke ferdig prossesert")
    }
}
