package no.nav.familie.ks.barnehagelister.rest

import jakarta.servlet.http.HttpServletRequest
import no.nav.familie.ks.barnehagelister.domene.mapTilSkjemaV1
import no.nav.familie.ks.barnehagelister.interceptor.hentSupplierId
import no.nav.familie.ks.barnehagelister.kontrakt.FormV1
import no.nav.familie.ks.barnehagelister.kontrakt.KindergartenlistResponse
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
        formV1: FormV1,
        bindingResult: BindingResult,
        request: HttpServletRequest,
    ): ResponseEntity<KindergartenlistResponse> {
        validerGodkjentLeverandør(request)

        return barnehagelisteService.mottaBarnehagelister(formV1.mapTilSkjemaV1(), bindingResult)
    }

    private fun validerGodkjentLeverandør(request: HttpServletRequest) {
        val supplierId = request.hentSupplierId() ?: throw UkjentLeverandørFeil("No supplier in request.")

        if (supplierId !in godkjenteLeverandører.leverandorer.map { it.orgno }) {
            throw UkjentLeverandørFeil("Supplier with orgno ${supplierId.substringAfter(":")} is not a known supplier.")
        }
    }

    override fun status(
        transaksjonsId: UUID,
        request: HttpServletRequest,
    ): ResponseEntity<KindergartenlistResponse> {
        validerGodkjentLeverandør(request)
        return barnehagelisteService.status(transaksjonsId)
    }

    override fun ping(): String = barnehagelisteService.ping()
}
