package no.nav.familie.ks.barnehagelister.domene

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisterRepository
import no.nav.familie.ks.barnehagelister.rest.dto.BarnehagelisteStatus
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class BarnehagelisteServiceTest {
    private val mockBarnehagelisterRepository = mockk<BarnehagelisterRepository>()
    private val mockTaskService = mockk<TaskService>()

    private val barnehagelisteService =
        BarnehagelisteService(
            barnehagelisterRepository = mockBarnehagelisterRepository,
            taskService = mockTaskService,
        )

    @Nested
    inner class MottaBarnehagelisterTest {
        @Test
        fun `Skal returnere allerede eksisterende barnehageliste hvis det er lagret fra før`() {
            // Arrange
            val uuid = UUID.randomUUID()

            val eksisterendeSkjemaV1 =
                SkjemaV1(
                    id = uuid,
                    barnehager = mockk(),
                    listeopplysninger = mockk(),
                )

            val lagretBarnehageliste = mockk<Barnehagelister>()

            every { mockBarnehagelisterRepository.findByIdOrNull(uuid) } returns lagretBarnehageliste

            // Act
            val barnehageliste =
                barnehagelisteService.mottaBarnehagelister(
                    eksisterendeSkjemaV1,
                    "testLeverandørOrgNr",
                    "testKommuneOrgNr",
                )

            // Assert
            verify(exactly = 1) { mockBarnehagelisterRepository.findByIdOrNull(uuid) }
            assertThat(barnehageliste).isEqualTo(lagretBarnehageliste)
        }

        @Test
        fun `Skal lagre barnehagelister og lage task på å prossere den hvis liste ikke finnes fra før`() {
            // Arrange
            val uuid = UUID.randomUUID()

            val ikkeEksisterendeSkjemaV1 =
                SkjemaV1(
                    id = uuid,
                    barnehager = mockk(),
                    listeopplysninger = mockk(),
                )

            every { mockBarnehagelisterRepository.findByIdOrNull(uuid) } returns null
            every { mockBarnehagelisterRepository.insert(any()) } returnsArgument 0
            every { mockTaskService.save(any()) } returnsArgument 0

            // Act
            val barnehageliste =
                barnehagelisteService.mottaBarnehagelister(
                    ikkeEksisterendeSkjemaV1,
                    "testLeverandørOrgNr",
                    "testKommuneOrgNr",
                )

            // Assert
            verify(exactly = 1) { mockBarnehagelisterRepository.findByIdOrNull(uuid) }
            verify { mockBarnehagelisterRepository.insert(barnehageliste) }
            verify(exactly = 1) { mockTaskService.save(any()) }

            assertThat(barnehageliste.id).isEqualTo(uuid)
            assertThat(barnehageliste.status).isEqualTo(BarnehagelisteStatus.MOTTATT)
        }
    }

    @Nested
    inner class HentBarnehagelisterTest {
        @Test
        fun `Skal returnere barnehagelister hvis det er lagret`() {
            // Arrange
            val uuid = UUID.randomUUID()

            val lagretBarnehageliste = mockk<Barnehagelister>()

            every { mockBarnehagelisterRepository.findByIdOrNull(uuid) } returns lagretBarnehageliste

            // Act
            val barnehageliste = barnehagelisteService.hentBarnehagelister(uuid)

            // Assert
            verify(exactly = 1) { mockBarnehagelisterRepository.findByIdOrNull(uuid) }
            assertThat(barnehageliste).isEqualTo(lagretBarnehageliste)
        }

        @Test
        fun `Skal returnere null hvis barnehagelister ikke er lagret fra før`() {
            // Arrange
            val uuid = UUID.randomUUID()

            every { mockBarnehagelisterRepository.findByIdOrNull(uuid) } returns null

            // Act
            val barnehageliste = barnehagelisteService.hentBarnehagelister(uuid)

            // Assert
            verify(exactly = 1) { mockBarnehagelisterRepository.findByIdOrNull(uuid) }
            assertThat(barnehageliste).isNull()
        }
    }
}
