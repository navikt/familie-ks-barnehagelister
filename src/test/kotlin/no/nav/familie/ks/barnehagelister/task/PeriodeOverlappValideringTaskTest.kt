package no.nav.familie.ks.barnehagelister.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ks.barnehagelister.domene.Barnehageliste
import no.nav.familie.ks.barnehagelister.domene.BarnehagelisteValideringsfeil
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisteRepository
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisteValideringsfeilRepository
import no.nav.familie.ks.barnehagelister.rest.dto.BarnehagelisteStatus
import no.nav.familie.ks.barnehagelister.rest.dto.EtterprosesseringfeilType
import no.nav.familie.ks.barnehagelister.testdata.FormV1RequestDtoTestData
import no.nav.familie.ks.barnehagelister.testdata.FormV1RequestDtoTestData.Companion.lagBarn
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate
import java.util.UUID

class PeriodeOverlappValideringTaskTest {
    val mockBarnehagelisteRepository = mockk<BarnehagelisteRepository>()
    val mockBarnehagelisteValideringsfeilRepository = mockk<BarnehagelisteValideringsfeilRepository>()
    val taskService = mockk<TaskService>()

    val periodeOverlappValideringTask =
        PeriodeOverlappValideringTask(
            barnehagelisteRepository = mockBarnehagelisteRepository,
            barnehagelisteValideringsfeilRepository = mockBarnehagelisteValideringsfeilRepository,
            taskService = taskService,
        )

    val barnehagelisteId = UUID.randomUUID()

    @Test
    fun `Task skal kjøre ok hvis det ikke er noen valideringsfeil`() {
        // Arrange
        val barnehageliste =
            Barnehageliste(
                id = barnehagelisteId,
                rawJson = FormV1RequestDtoTestData.lagRequest(),
                status = BarnehagelisteStatus.MOTTATT,
            )
        every { mockBarnehagelisteRepository.findByIdOrNull(barnehagelisteId) } returns barnehageliste
        // Act
        periodeOverlappValideringTask.doTask(PeriodeOverlappValideringTask.opprettTask(barnehagelisteId.toString()))

        // Assert
        verify(exactly = 0) { mockBarnehagelisteValideringsfeilRepository.insertAll(any()) }
    }

    @Test
    fun `Task skal lagre ned valideringsfeil hvis det er en perioder som overlapper`() {
        // Arrange
        val skjema =
            FormV1RequestDtoTestData.lagRequest().copy(
                kindergartens =
                    listOf(
                        FormV1RequestDtoTestData.lagBarnehage().copy(
                            childrenInformation =
                                listOf(
                                    FormV1RequestDtoTestData.lagBarnInfolinje().copy(
                                        startDate = LocalDate.of(2025, 1, 1),
                                        endDate = LocalDate.of(2025, 7, 31),
                                    ),
                                    FormV1RequestDtoTestData.lagBarnInfolinje().copy(
                                        startDate = LocalDate.of(2025, 6, 1),
                                        endDate = LocalDate.of(2025, 10, 31),
                                    ),
                                ),
                        ),
                    ),
            )

        val barnehageliste =
            Barnehageliste(
                id = barnehagelisteId,
                rawJson = skjema,
                status = BarnehagelisteStatus.MOTTATT,
            )
        every { mockBarnehagelisteRepository.findByIdOrNull(barnehagelisteId) } returns barnehageliste

        val slot = slot<List<BarnehagelisteValideringsfeil>>()
        every { mockBarnehagelisteValideringsfeilRepository.insertAll(capture(slot)) } answers { slot.captured }

        // Act
        periodeOverlappValideringTask.doTask(PeriodeOverlappValideringTask.opprettTask(barnehagelisteId.toString()))

        // Assert
        verify(exactly = 1) { mockBarnehagelisteValideringsfeilRepository.insertAll(any()) }
        assertThat(slot.captured).hasSize(1)
        assertThat(slot.captured.first().ident).isEqualTo(lagBarn().socialSecurityNumber)
        assertThat(slot.captured.first().etterprosesseringfeiltype).isEqualTo(EtterprosesseringfeilType.OVERLAPPING_PERIOD_WITHIN_SAME_LIST)
        assertThat(slot.captured.first().feilinfo).isEqualTo("Overlapping period within the same list for children.")
    }
}
