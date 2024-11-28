package no.nav.familie.ks.barnehagelister.rest

import jakarta.servlet.http.HttpServletRequest
import no.nav.familie.ks.barnehagelister.interceptor.hentSupplierId
import no.nav.familie.ks.barnehagelister.kontrakt.SkjemaV1
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Profile("!dev")
@RestController
class DefaultBarnehagelisterController(
    private val barnehagelisteService: BarnehagelisteService,
    private val godkjenteLeverandører: GodkjenteLeverandører,
) : BarnehagelisterController {
    override fun mottaBarnehagelister(
        skjemaV1: SkjemaV1,
        bindingResult: BindingResult,
        request: HttpServletRequest,
    ): ResponseEntity<BarnehagelisteResponse> {
        val supplierId = request.hentSupplierId()
        if (supplierId !in godkjenteLeverandører.ider) {
            throw JwtTokenUnauthorizedException("Leverandør med orgnr ${supplierId.substringAfter(":")} er ikke en godkjent leverandør")
        }

        return barnehagelisteService.mottaBarnehagelister(skjemaV1, bindingResult)
    }

    override fun status(transaksjonsId: UUID): ResponseEntity<BarnehagelisteResponse> = barnehagelisteService.status(transaksjonsId)

    override fun ping(): String = barnehagelisteService.ping()
}
