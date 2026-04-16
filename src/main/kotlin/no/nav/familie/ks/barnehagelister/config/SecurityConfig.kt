package no.nav.familie.ks.barnehagelister.config

import no.nav.familie.ks.barnehagelister.security.MaskinportenAuthenticationManager
import no.nav.familie.prosessering.config.ProsesseringInfoProvider
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
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.springframework.security.web.util.matcher.NegatedRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("!dev")
class SecurityConfig(
    private val maskinportenAuthenticationManager: MaskinportenAuthenticationManager,
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
                authorize(anyRequest, authenticated)
            }
            oauth2ResourceServer {
                jwt {
                    authenticationManager = maskinportenAuthenticationManager
                }
            }
            csrf { disable() }
        }
        // Forsikre oss om at prosessering-spring-security tar seg av /task/api/**
        http.securityMatcher(NegatedRequestMatcher(PathPatternRequestMatcher.pathPattern("/api/task/**")))
        return http.build()
    }

    @Bean
    fun prosesseringInfoProvider(
        @Value("\${prosessering.rolle}") prosesseringRolle: String,
    ) = object : ProsesseringInfoProvider {
        override fun hentBrukernavn(): String =
            try {
                hentJwt()?.getClaimAsString("preferred_username") ?: "VL"
            } catch (e: Exception) {
                "VL"
            }

        override fun harTilgang(): Boolean = grupper().contains(prosesseringRolle)

        private fun grupper(): List<String> =
            try {
                hentJwt()?.getClaimAsStringList("groups") ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

        private fun hentJwt() = (SecurityContextHolder.getContext()?.authentication as? JwtAuthenticationToken)?.token
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
