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
class DefaultBarnehagelisteController(
    private val barnehagelisteService: BarnehagelisteService,
    private val godkjenteLeverandører: GodkjenteLeverandører,
) : BarnehagelisteController {
    override fun mottaBarnehageliste(
        formV1RequestDto: FormV1RequestDto,
        bindingResult: BindingResult,
        request: HttpServletRequest,
    ): ResponseEntity<KindergartenlistResponse> {
        validerGodkjentLeverandør(request)
        bindingResult.kastValideringsfeilHvisValideringFeiler()

        val leverandørOrgNr = request.hentSupplierId() ?: error("No supplier in request.")

        val barnehageliste =
            barnehagelisteService.mottaBarnehageliste(formV1RequestDto.mapTilSkjemaV1(), leverandørOrgNr)

        return barnehageliste.tilKindergartenlistResponse().toResponseEntity()
    }

    override fun status(
        id: UUID,
        request: HttpServletRequest,
    ): ResponseEntity<KindergartenlistResponse> {
        validerGodkjentLeverandør(request)

        val barnehageliste = barnehagelisteService.hentBarnehageliste(id)

        val leverandørOrgNr = request.hentSupplierId() ?: error("No supplier in request.")
        val kommunenummer = request.hentConsumerId() ?: error("No municipality in request.")

        return when {
            barnehageliste != null && barnehageliste.leverandorOrgNr != leverandørOrgNr ->
                throw UgyldigKommuneEllerLeverandørFeil("The requested kindergarten list were not sent in by supplier $leverandørOrgNr")

            barnehageliste != null && kommunenummer != barnehageliste.rawJson.listeopplysninger.kommunenummer ->
                throw UgyldigKommuneEllerLeverandørFeil("The requested kindergarten list were not sent in by municipality $kommunenummer")

            else ->
                barnehageliste
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
