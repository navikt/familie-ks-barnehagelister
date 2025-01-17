package no.nav.familie.ks.barnehagelister.rest

import jakarta.servlet.http.HttpServletRequest
import no.nav.familie.ks.barnehagelister.domene.BarnehagelisteService
import no.nav.familie.ks.barnehagelister.domene.tilKindergartenlistResponse
import no.nav.familie.ks.barnehagelister.interceptor.hentConsumerId
import no.nav.familie.ks.barnehagelister.interceptor.hentSupplierId
import no.nav.familie.ks.barnehagelister.rest.dto.FormV1RequestDto
import no.nav.familie.ks.barnehagelister.rest.dto.KindergartenlistResponse
import no.nav.familie.ks.barnehagelister.rest.dto.mapTilSkjemaV1
import no.nav.familie.ks.barnehagelister.rest.dto.toResponseEntity
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Profile("!dev")
@RestController
class DefaultBarnehagelisterController(
    private val barnehagelisteService: BarnehagelisteService,
    private val godkjenteLeverandører: GodkjenteLeverandører,
) : BarnehagelisterController {
    override fun mottaBarnehagelister(
        formV1RequestDto: FormV1RequestDto,
        bindingResult: BindingResult,
        request: HttpServletRequest,
    ): ResponseEntity<KindergartenlistResponse> {
        validerGodkjentLeverandør(request)
        bindingResult.kastValideringsfeilHvisValideringFeiler()

        val leverandørOrgNr = request.hentSupplierId() ?: error("No supplier in request.")
        val kommuneOrgNr = request.hentConsumerId() ?: error("No municipality in request.")

        val barnehagelister =
            barnehagelisteService.mottaBarnehagelister(formV1RequestDto.mapTilSkjemaV1(), leverandørOrgNr, kommuneOrgNr)

        return barnehagelister.tilKindergartenlistResponse().toResponseEntity()
    }

    override fun status(
        id: UUID,
        request: HttpServletRequest,
    ): ResponseEntity<KindergartenlistResponse> {
        validerGodkjentLeverandør(request)

        val barnehagelister = barnehagelisteService.hentBarnehagelister(id)

        val leverandørOrgNr = request.hentSupplierId() ?: error("No supplier in request.")
        val kommuneOrgNr = request.hentConsumerId() ?: error("No municipality in request.")

        return when {
            barnehagelister != null && barnehagelister.leverandorOrgNr != leverandørOrgNr ->
                throw UgyldigKommuneEllerLeverandørFeil("The requested kindergarten list were not sent in by supplier $leverandørOrgNr")

            barnehagelister != null && barnehagelister.kommuneOrgNr != kommuneOrgNr ->
                throw UgyldigKommuneEllerLeverandørFeil(
                    "The requested kindergarten list were not sent in by municipality with org id $kommuneOrgNr",
                )

            else ->
                barnehagelister
                    ?.tilKindergartenlistResponse()
                    ?.toResponseEntity()
                    ?: ResponseEntity.notFound().build()
        }
    }

    override fun ping(): String = "\"OK\""

    private fun validerGodkjentLeverandør(request: HttpServletRequest) {
        val supplierId = request.hentSupplierId() ?: throw UgyldigKommuneEllerLeverandørFeil("No supplier in request.")

        if (supplierId !in godkjenteLeverandører.leverandorer.map { it.orgno }) {
            throw UgyldigKommuneEllerLeverandørFeil("Supplier with orgno ${supplierId.substringAfter(":")} is not a known supplier.")
        }
    }

    private fun BindingResult.kastValideringsfeilHvisValideringFeiler() {
        if (hasErrors()) {
            val feil =
                allErrors.map {
                    if (it is FieldError) {
                        ValideringsfeilInfo(it.field, it.defaultMessage ?: "mangler")
                    } else {
                        ValideringsfeilInfo("mangler", it.defaultMessage ?: "mangler")
                    }
                }
            throw ValideringsfeilException(feil)
        }
    }
}
