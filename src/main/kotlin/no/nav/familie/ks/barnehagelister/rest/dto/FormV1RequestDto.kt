package no.nav.familie.ks.barnehagelister.rest.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import no.nav.familie.ks.barnehagelister.domene.Barnehagebarn
import java.util.UUID

// TODO sanitize JSON input according to OWASP
@Schema(description = "Model for submitting kindergarten lists", name = "FormV1")
data class FormV1RequestDto(
    @Schema(
        description = "Identifier for the submitted request. Is used to check the status of the request.",
        example = "19375e59-0f07-4c9b-a7bb-6f30fb43819b",
    )
    @field:NotNull
    val id: UUID,
    @field:Valid
    val kindergartens: List<KindergartenRequestDto>?,
    @field:NotNull
    @field:Valid
    val listInformation: ListInformationRequestDto,
)

fun FormV1RequestDto.mapTilBarnehagebarn(): List<Barnehagebarn> =
    kindergartens
        .orEmpty()
        .flatMap { dto ->
            dto.childrenInformation.map { barnInfoLinje ->
                Barnehagebarn(
                    ident = barnInfoLinje.child.socialSecurityNumber,
                    fom = barnInfoLinje.startDate,
                    tom = barnInfoLinje.endDate,
                    antallTimerIBarnehage = barnInfoLinje.agreedHoursInKindergarten,
                    kommuneNavn = listInformation.municipalityName,
                    kommuneNr = listInformation.municipalityNumber,
                    barnehagelisteId = id,
                    organisasjonsnummer = dto.organizationNumber,
                )
            }
        }
