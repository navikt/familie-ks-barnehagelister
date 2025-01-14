package no.nav.familie.ks.barnehagelister.task

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.familie.ks.barnehagelister.domene.Barnehageliste
import no.nav.familie.ks.barnehagelister.domene.BarnehagelisteService
import no.nav.familie.ks.barnehagelister.kafka.DummyBarnehagebarnKafkaProducer
import no.nav.familie.ks.barnehagelister.repository.BarnehagebarnRepository
import no.nav.familie.ks.barnehagelister.rest.dto.BarnehagelisteStatus
import no.nav.familie.ks.barnehagelister.testdata.SkjemaV1TestData
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

    val sendBarnehagebarnTilKsTask =
        SendBarnehagebarnTilKsTask(
            barnehagebarnRepository = barnehagebarnRepository,
            barnehagelisteService = barnehagelisteService,
            barnehagebarnKafkaProducer = barnehagebarnKafkaProducer,
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
        val mockBarnehageliste =
            Barnehageliste(
                id = barnehagelisteId,
                rawJson = SkjemaV1TestData.lagSkjemaV1(),
                status = BarnehagelisteStatus.FERDIG,
            )

        val mockBarnehagebarn =
            SkjemaV1TestData.lagTilhørendeBarnehageBarnKs(barnehagelisteId)

        every { barnehagebarnRepository.findByIdOrNull(barnehagelisteId) } returns mockBarnehagebarn
        every { barnehagelisteService.hentBarnehageliste(barnehagelisteId) } returns mockBarnehageliste

        val sendBarnehagelisteTask = SendBarnehagebarnTilKsTask.opprettTask(barnehagelisteId)

        // Act
        sendBarnehagebarnTilKsTask.doTask(sendBarnehagelisteTask)

        // Assert
        verify(exactly = 1) { barnehagebarnKafkaProducer.sendBarnehageBarn(mockBarnehagebarn) }
    }

    @Test
    fun `Skal kaste feil hvis barnehagelisten ikke er ferdig`() {
        // Arrange
        val mockBarnehageliste =
            Barnehageliste(
                id = barnehagelisteId,
                rawJson = SkjemaV1TestData.lagSkjemaV1(),
                status = BarnehagelisteStatus.MOTTATT,
            )

        val mockBarnehagebarn =
            SkjemaV1TestData.lagTilhørendeBarnehageBarnKs(barnehagelisteId)

        every { barnehagelisteService.hentBarnehageliste(barnehagelisteId) } returns mockBarnehageliste
        every { barnehagebarnRepository.findByIdOrNull(barnehagelisteId) } returns mockBarnehagebarn

        val sendBarnehagelisteTask = SendBarnehagebarnTilKsTask.opprettTask(barnehagelisteId)

        // Act && Assert
        val exception =
            assertThrows<IllegalStateException> {
                sendBarnehagebarnTilKsTask.doTask(sendBarnehagelisteTask)
            }

        // Assert
        assertThat(exception.message).isEqualTo("Barnehageliste med id $barnehagelisteId er ikke ferdig prossesert")
    }
}
