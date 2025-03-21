package no.nav.familie.ks.barnehagelister.rest

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "godkjente-leverandorer")
data class GodkjenteLeverandører(
    var leverandorer: List<Leverandør> = emptyList(),
)

data class Leverandør(
    var orgno: String,
    var navn: String,
)

class UgyldigKommuneEllerLeverandørFeil(
    message: String?,
) : Exception(message)
