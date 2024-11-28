package no.nav.familie.ks.barnehagelister.rest

import jakarta.servlet.http.HttpServletRequest
import no.nav.familie.ks.barnehagelister.interceptor.hentSupplierId
import no.nav.familie.ks.barnehagelister.kontrakt.SkjemaV1
import no.nav.security.token.support.core.api.Unprotected
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Profile("dev")
@RestController
class UnprotectedBarnehagelisteController(
    private val barnehagelisteService: BarnehagelisteService,
    private val godkjenteLeverandører: GodkjenteLeverandører,
) : BarnehagelisterController {
    @Unprotected
    override fun mottaBarnehagelister(
        skjemaV1: SkjemaV1,
        bindingResult: BindingResult,
        request: HttpServletRequest,
    ): ResponseEntity<BarnehagelisteResponse> {
        val supplierId = request.hentSupplierId()
        if (supplierId !in godkjenteLeverandører.ider) {
            throw JwtTokenUnauthorizedException("Leverandør med id $supplierId er ikke en godkjent leverandør")
        }
        return barnehagelisteService.mottaBarnehagelister(skjemaV1, bindingResult)
    }

    @Unprotected
    override fun status(transaksjonsId: UUID): ResponseEntity<BarnehagelisteResponse> = barnehagelisteService.status(transaksjonsId)

    @Unprotected
    override fun ping(): String = barnehagelisteService.ping()
}
