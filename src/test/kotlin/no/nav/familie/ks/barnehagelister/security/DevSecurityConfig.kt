package no.nav.familie.ks.barnehagelister.security

import no.nav.familie.prosessering.config.ProsesseringInfoProvider
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("dev")
class DevSecurityConfig(
    private val maskinportenJwtAuthenticationConverter: MaskinportenJwtAuthenticationConverter,
) {
    @Bean
    open fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("/internal/**", permitAll)
                authorize("/actuator/**", permitAll)
                authorize("/swagger-ui/**", permitAll)
                authorize("/v3/api-docs/**", permitAll)
                authorize("/swagger-ui.html", permitAll)
                authorize("/tables", permitAll)
                authorize("/testtoken/**", permitAll)
                authorize("/api/kindergartenlists/**", permitAll)
                authorize(anyRequest, authenticated)
            }
            csrf { disable() }
        }
        return http.build()
    }

    // TODO: må finne ut hva vi gjør med prosessering når vi kjører lokalt
    @Bean
    fun prosesseringInfoProvider(
        @Value("\${prosessering.rolle}") prosesseringRolle: String,
    ) = object : ProsesseringInfoProvider {
        override fun hentBrukernavn(): String =
            try {
                SpringTokenValidationContextHolder()
                    .getTokenValidationContext()
                    .getClaims("azuread")
                    .getStringClaim("preferred_username")
            } catch (e: Exception) {
                "VL"
            }

        override fun harTilgang(): Boolean = grupper().contains(prosesseringRolle)

        @Suppress("UNCHECKED_CAST")
        private fun grupper(): List<String> =
            try {
                SpringTokenValidationContextHolder()
                    .getTokenValidationContext()
                    .getClaims("azuread")
                    .get("groups") as List<String>? ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
    }
}
