package no.nav.familie.ks.barnehagelister.task

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.familie.ks.barnehagelister.domene.Barnehagelister
import no.nav.familie.ks.barnehagelister.kafka.DummyBarnehagebarnKafkaProducer
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisterRepository
import no.nav.familie.ks.barnehagelister.rest.dto.BarnehagelisteStatus
import no.nav.familie.ks.barnehagelister.testdata.SkjemaV1TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class SendBarnehagelisteTilKsTaskTest {
    val barnehagelisterRepository = mockk<BarnehagelisterRepository>()
    val barnehagebarnKafkaProducer = mockk<DummyBarnehagebarnKafkaProducer>()

    val sendBarnehagelisteTilKsTask =
        SendBarnehagelisteTilKsTask(
            barnehagelisterRepository = barnehagelisterRepository,
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
        val mockBarnehagelister =
            Barnehagelister(
                id = barnehagelisteId,
                rawJson = SkjemaV1TestData.lagSkjemaV1(),
                status = BarnehagelisteStatus.FERDIG,
            )

        every { barnehagelisterRepository.findByIdOrNull(barnehagelisteId) } returns mockBarnehagelister

        val sendBarnehagelisteTask = SendBarnehagelisteTilKsTask.opprettTask(barnehagelisteId)

        // Act
        sendBarnehagelisteTilKsTask.doTask(sendBarnehagelisteTask)

        // Assert
        verify(exactly = 1) { barnehagebarnKafkaProducer.sendBarnehageBarn(any()) }
    }

    @Test
    fun `Skal kaste feil hvis barnehagelisten ikke er ferdig`() {
        // Arrange
        val mockBarnehagelister =
            Barnehagelister(
                id = barnehagelisteId,
                rawJson = SkjemaV1TestData.lagSkjemaV1(),
                status = BarnehagelisteStatus.MOTTATT,
            )

        every { barnehagelisterRepository.findByIdOrNull(barnehagelisteId) } returns mockBarnehagelister

        val sendBarnehagelisteTask = SendBarnehagelisteTilKsTask.opprettTask(barnehagelisteId)

        // Act && Assert
        val exception =
            assertThrows<IllegalStateException> {
                sendBarnehagelisteTilKsTask.doTask(sendBarnehagelisteTask)
            }

        // Assert
        assertThat(exception.message).isEqualTo("Barnehageliste med id $barnehagelisteId er ikke ferdig prossesert")
    }

    @Test
    fun `Skal ikke legge barnehagebarn på kø når det ikke er noen barnehage`() {
        // Arrange
        val mockBarnehagelister =
            Barnehagelister(
                id = barnehagelisteId,
                rawJson = SkjemaV1TestData.lagSkjemaV1().copy(barnehager = emptyList()),
                status = BarnehagelisteStatus.FERDIG,
            )

        every { barnehagelisterRepository.findByIdOrNull(barnehagelisteId) } returns mockBarnehagelister

        val sendBarnehagelisteTask = SendBarnehagelisteTilKsTask.opprettTask(barnehagelisteId)

        // Act
        sendBarnehagelisteTilKsTask.doTask(sendBarnehagelisteTask)

        // Assert
        verify(exactly = 0) { barnehagebarnKafkaProducer.sendBarnehageBarn(any()) }
    }
}
