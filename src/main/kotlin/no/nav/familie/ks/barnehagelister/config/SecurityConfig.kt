package no.nav.familie.ks.barnehagelister.config

import no.nav.familie.ks.barnehagelister.security.MaskinportenJwtAuthenticationConverter
import no.nav.familie.prosessering.config.ProsesseringInfoProvider
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("!dev")
class SecurityConfig(
    private val maskinportenJwtAuthenticationConverter: MaskinportenJwtAuthenticationConverter,
) {
    @Bean
    open fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            cors { configurationSource = corsConfigurationSource() }
            authorizeHttpRequests {
                authorize("/internal/**", permitAll)
                authorize("/actuator/**", permitAll)
                authorize("/swagger-ui/**", permitAll)
                authorize("/v3/api-docs/**", permitAll)
                authorize("/swagger-ui.html", permitAll)
                authorize("/tables", permitAll)
                authorize("/testtoken/**", permitAll)
                authorize(anyRequest, authenticated)
            }
            oauth2ResourceServer {
                jwt {
                    jwtAuthenticationConverter = maskinportenJwtAuthenticationConverter
                }
            }
            csrf { disable() }
        }
        return http.build()
    }

    // TODO: kan dette fjernes?
    open fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        configuration.allowedOrigins =
            listOf(
                "https://familie-ks-infotrygd.intern.dev.nav.no",
                "https://familie-ks-infotrygd.intern.nav.no",
                "http://localhost:8080",
            )

        configuration.allowedMethods = listOf("GET", "POST", "OPTIONS")
        configuration.allowedHeaders =
            listOf(
                "Content-Type",
                "Accept",
                "Authorization",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
            )
        configuration.allowCredentials = true
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

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

    @Bean
    fun securityHeaderFilter(): FilterRegistrationBean<SecurityHeaderFilter> {
        log.info("Registering SecurityHeaderFilter filter")
        val filterRegistration: FilterRegistrationBean<SecurityHeaderFilter> = FilterRegistrationBean()
        filterRegistration.setFilter(SecurityHeaderFilter())
        filterRegistration.order = 2
        return filterRegistration
    }

    companion object {
        private val log = LoggerFactory.getLogger(SecurityConfig::class.java)
    }
}
