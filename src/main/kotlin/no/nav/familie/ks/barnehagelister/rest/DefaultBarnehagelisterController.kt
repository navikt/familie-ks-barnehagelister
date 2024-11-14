package no.nav.familie.ks.barnehagelister.rest

import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import no.nav.familie.ks.barnehagelister.kontrakt.SkjemaV1
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Profile("!dev")
@RestController
class DefaultBarnehagelisterController(
    private val barnehagelisteService: BarnehagelisteService,
    private val observationRegistry: ObservationRegistry,
) : BarnehagelisterController {
    override fun mottaBarnehagelister(
        skjemaV1: SkjemaV1,
        bindingResult: BindingResult,
    ): ResponseEntity<BarnehagelisteResponse> =
        Observation
            .createNotStarted("service.mottaBarnehagelister", observationRegistry)
            .observeNotNull {
                barnehagelisteService.mottaBarnehagelister(skjemaV1, bindingResult)
            }

    override fun status(transaksjonsId: UUID): ResponseEntity<BarnehagelisteResponse> = barnehagelisteService.status(transaksjonsId)

    override fun ping(): String = barnehagelisteService.ping()

    fun <T : Any> Observation.observeNotNull(block: () -> T): T = observeBlock(block)

    fun <T : Any?> Observation.observeNullable(block: () -> T): T = observeBlock(block)

    private fun <T> Observation.observeBlock(block: () -> T): T {
        start()
        return try {
            openScope().use {
                block()
            }
        } catch (error: Throwable) {
            error(error)
            throw error
        } finally {
            stop()
        }
    }
}
