package no.nav.familie.ks.barnehagelister.rest

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Component
@Configuration
@ConfigurationProperties(prefix = "godkjente-leverandorer")
data class GodkjenteLeverandører(
    var ider: List<String> = emptyList(),
)
