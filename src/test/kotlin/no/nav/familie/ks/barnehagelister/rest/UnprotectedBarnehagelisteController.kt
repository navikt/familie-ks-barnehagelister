package no.nav.familie.ks.barnehagelister.rest

import jakarta.servlet.http.HttpServletRequest
import no.nav.familie.ks.barnehagelister.domene.mapTilSkjemaV1
import no.nav.familie.ks.barnehagelister.kontrakt.FormV1
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Profile("dev")
@RestController
class UnprotectedBarnehagelisteController(
    private val barnehagelisteService: BarnehagelisteService,
) : BarnehagelisterController {
    @Unprotected
    override fun mottaBarnehagelister(
        formV1: FormV1,
        bindingResult: BindingResult,
        request: HttpServletRequest,
    ): ResponseEntity<BarnehagelisteResponse> = barnehagelisteService.mottaBarnehagelister(formV1.mapTilSkjemaV1(), bindingResult)

    @Unprotected
    override fun status(
        transaksjonsId: UUID,
        request: HttpServletRequest,
    ): ResponseEntity<BarnehagelisteResponse> = barnehagelisteService.status(transaksjonsId)

    @Unprotected
    override fun ping(): String = barnehagelisteService.ping()
}
