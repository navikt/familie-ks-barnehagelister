package no.nav.familie.ks.barnehagelister.domene

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisteRepository
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisteValideringsfeilRepository
import no.nav.familie.ks.barnehagelister.rest.dto.BarnehagelisteStatus
import no.nav.familie.ks.barnehagelister.rest.dto.FormV1RequestDto
import no.nav.familie.ks.barnehagelister.service.BarnehagelisteService
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class BarnehagelisteServiceTest {
    private val mockBarnehagelisteRepository = mockk<BarnehagelisteRepository>()
    private val mockBarnehagelisteValideringsfeilRepository = mockk<BarnehagelisteValideringsfeilRepository>()
    private val mockTaskService = mockk<TaskService>()

    private val barnehagelisteService =
        BarnehagelisteService(
            barnehagelisteRepository = mockBarnehagelisteRepository,
            taskService = mockTaskService,
            barnehagelisteValideringsfeilRepository = mockBarnehagelisteValideringsfeilRepository,
        )

    @Nested
    inner class MottaBarnehagelisteTest {
        @Test
        fun `Skal returnere allerede eksisterende barnehageliste hvis det er lagret fra før`() {
            // Arrange
            val uuid = UUID.randomUUID()

            val eksisterendeRequest =
                FormV1RequestDto(
                    id = uuid,
                    kindergartens = mockk(),
                    listInformation = mockk(),
                )

            val lagretBarnehageliste =
                Barnehageliste(
                    id = uuid,
                    rawJson = eksisterendeRequest,
                    status = BarnehagelisteStatus.MOTTATT,
                    leverandorOrgNr = "123456789",
                    kommuneOrgNr = "123456789",
                )

            every { mockBarnehagelisteRepository.findByIdOrNull(uuid) } returns lagretBarnehageliste
            every { mockBarnehagelisteValideringsfeilRepository.findByBarnehagelisteId(uuid) } returns emptyList()

            // Act
            val barnehageliste =
                barnehagelisteService.mottaBarnehageliste(
                    eksisterendeRequest,
                    "testLeverandørOrgNr",
                    "testKommuneOrgNr",
                )

            // Assert
            verify(exactly = 1) { mockBarnehagelisteRepository.findByIdOrNull(uuid) }
            assertThat(barnehageliste.barnehageliste).isEqualTo(lagretBarnehageliste)
        }

        @Test
        fun `Skal lagre barnehagelister og lage task på å prossere den hvis liste ikke finnes fra før`() {
            // Arrange
            val uuid = UUID.randomUUID()

            val ikkeEksisterendeRequest =
                FormV1RequestDto(
                    id = uuid,
                    kindergartens = mockk(),
                    listInformation = mockk(),
                )

            every { mockBarnehagelisteRepository.findByIdOrNull(uuid) } returns null
            every { mockBarnehagelisteRepository.insert(any()) } returnsArgument 0
            every { mockTaskService.save(any()) } returnsArgument 0
            every { mockBarnehagelisteValideringsfeilRepository.findByBarnehagelisteId(uuid) } returns emptyList()

            // Act
            val barnehagelisteMedValideringsfeil =
                barnehagelisteService.mottaBarnehageliste(
                    ikkeEksisterendeRequest,
                    "testLeverandørOrgNr",
                    "testKommuneOrgNr",
                )

            // Assert
            val barnehageliste = barnehagelisteMedValideringsfeil.barnehageliste!!
            verify(exactly = 1) { mockBarnehagelisteRepository.findByIdOrNull(uuid) }
            verify { mockBarnehagelisteRepository.insert(barnehageliste) }
            verify(exactly = 1) { mockTaskService.save(any()) }

            assertThat(barnehageliste.id).isEqualTo(uuid)
            assertThat(barnehageliste.status).isEqualTo(BarnehagelisteStatus.MOTTATT)
        }
    }

    @Nested
    inner class HentBarnehagelisteTest {
        @Test
        fun `Skal returnere barnehagelister hvis det er lagret`() {
            // Arrange
            val uuid = UUID.randomUUID()

            val lagretBarnehageliste = mockk<Barnehageliste>()

            every { mockBarnehagelisteRepository.findByIdOrNull(uuid) } returns lagretBarnehageliste

            // Act
            val barnehageliste = barnehagelisteService.hentBarnehageliste(uuid)

            // Assert
            verify(exactly = 1) { mockBarnehagelisteRepository.findByIdOrNull(uuid) }
            assertThat(barnehageliste).isEqualTo(lagretBarnehageliste)
        }

        @Test
        fun `Skal returnere null hvis barnehagelister ikke er lagret fra før`() {
            // Arrange
            val uuid = UUID.randomUUID()

            every { mockBarnehagelisteRepository.findByIdOrNull(uuid) } returns null

            // Act
            val barnehageliste = barnehagelisteService.hentBarnehageliste(uuid)

            // Assert
            verify(exactly = 1) { mockBarnehagelisteRepository.findByIdOrNull(uuid) }
            assertThat(barnehageliste).isNull()
        }
    }
}
