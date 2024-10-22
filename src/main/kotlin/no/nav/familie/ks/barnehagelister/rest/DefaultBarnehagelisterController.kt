package no.nav.familie.ks.barnehagelister.rest

import no.nav.familie.ks.barnehagelister.kontrakt.SkjemaV1
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Profile("!dev")
@RestController
class DefaultBarnehagelisterController(
    private val barnehagelisteService: BarnehagelisteService,
) : BarnehagelisterController {
    override fun mottaBarnehagelister(skjemaV1: SkjemaV1): ResponseEntity<BarnehagelisteResponse> =
        barnehagelisteService.mottaBarnehagelister(skjemaV1)

    override fun status(transaksjonsId: UUID): ResponseEntity<BarnehagelisteResponse> = barnehagelisteService.status(transaksjonsId)

    override fun ping(): String = barnehagelisteService.ping()
}
