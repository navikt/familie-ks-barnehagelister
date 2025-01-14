package no.nav.familie.ks.barnehagelister.task

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.familie.ks.barnehagelister.domene.Barnehageliste
import no.nav.familie.ks.barnehagelister.domene.BarnehagelisteService
import no.nav.familie.ks.barnehagelister.repository.BarnehagebarnRepository
import no.nav.familie.ks.barnehagelister.rest.dto.BarnehagelisteStatus
import no.nav.familie.ks.barnehagelister.testdata.SkjemaV1TestData
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class LesBarnehagelisteTaskTest {
    val barnehagelisteService = mockk<BarnehagelisteService>()
    val barnehagebarnRepository = mockk<BarnehagebarnRepository>()
    val taskService = mockk<TaskService>()

    val lesBarnehagelisteTask =
        LesBarnehagelisteTask(
            barnehagebarnRepository = barnehagebarnRepository,
            taskService = taskService,
            barnehagelisteService = barnehagelisteService,
        )

    val barnehagelisteId = UUID.randomUUID()

    @BeforeEach()
    fun mockkReturns() {
        clearAllMocks()
        every { barnehagebarnRepository.insertAll(any()) } returns emptyList()
    }

    @Test
    fun `skal mappe om barnehageliste til barnehagebarn og lagre i database`() {
        val mockBarnehageliste =
            Barnehageliste(
                id = barnehagelisteId,
                rawJson = SkjemaV1TestData.lagSkjemaV1(),
                status = BarnehagelisteStatus.MOTTATT,
            )
        val mockTask = Task("", "")

        every { barnehagelisteService.hentBarnehageliste(barnehagelisteId) } returns mockBarnehageliste
        every { barnehagelisteService.settBarnehagelisteStatusTilFerdig(mockBarnehageliste) } just runs
        every { taskService.save(any()) } returns mockTask

        val task = LesBarnehagelisteTask.opprettTask(barnehagelisteId)

        // Act
        lesBarnehagelisteTask.doTask(task)

        // Assert
        verify(exactly = 1) {
            barnehagebarnRepository.insertAll(any())
        }
    }

    @Test
    fun `skal ikke lagre barnehagebarn hvis barnehagelisten har status FERDIG`() {
        // Arrange
        val mockBarnehageliste =
            Barnehageliste(
                id = barnehagelisteId,
                rawJson = SkjemaV1TestData.lagSkjemaV1(),
                status = BarnehagelisteStatus.FERDIG,
            )

        every { barnehagelisteService.hentBarnehageliste(barnehagelisteId) } returns mockBarnehageliste

        val task = LesBarnehagelisteTask.opprettTask(barnehagelisteId)

        // Act
        lesBarnehagelisteTask.doTask(task)

        // Assert
        verify(exactly = 0) {
            barnehagebarnRepository.insertAll(any())
        }
    }
}
