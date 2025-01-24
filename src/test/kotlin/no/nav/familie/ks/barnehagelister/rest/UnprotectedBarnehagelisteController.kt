package no.nav.familie.ks.barnehagelister.rest

import jakarta.servlet.http.HttpServletRequest
import no.nav.familie.ks.barnehagelister.domene.tilKindergartenlistResponse
import no.nav.familie.ks.barnehagelister.rest.dto.FormV1RequestDto
import no.nav.familie.ks.barnehagelister.rest.dto.KindergartenlistResponse
import no.nav.familie.ks.barnehagelister.rest.dto.mapTilSkjemaV1
import no.nav.familie.ks.barnehagelister.rest.dto.toResponseEntity
import no.nav.familie.ks.barnehagelister.service.BarnehagelisteService
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Profile("dev")
@RestController
class UnprotectedBarnehagelisteController(
    private val barnehagelisteService: BarnehagelisteService,
) : BarnehagelisteController {
    @Unprotected
    override fun mottaBarnehageliste(
        formV1RequestDto: FormV1RequestDto,
        bindingResult: BindingResult,
        request: HttpServletRequest,
    ): ResponseEntity<KindergartenlistResponse> {
        bindingResult.kastValideringsfeilHvisValideringFeiler()

        val barnehagelisteMedValideringsfeil =
            barnehagelisteService.mottaBarnehageliste(
                formV1RequestDto.mapTilSkjemaV1(),
                "testLeverandørOrgNr",
                "testKommuneOrgNr",
            )

        return barnehagelisteMedValideringsfeil.barnehageliste!!
            .tilKindergartenlistResponse(
                barnehagelisteMedValideringsfeil.valideringsfeil,
            ).toResponseEntity()
    }

    @Unprotected
    override fun status(
        id: UUID,
        request: HttpServletRequest,
    ): ResponseEntity<KindergartenlistResponse> {
        val barnehagelisteMedValideringsfeil = barnehagelisteService.hentBarnehagelisteMedValideringsfeil(id)
        return barnehagelisteService
            .hentBarnehagelisteMedValideringsfeil(id)
            .barnehageliste
            ?.tilKindergartenlistResponse(barnehagelisteMedValideringsfeil.valideringsfeil)
            ?.toResponseEntity()
            ?: ResponseEntity.notFound().build()
    }

    @Unprotected
    override fun ping(): String = "\"OK\""
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
