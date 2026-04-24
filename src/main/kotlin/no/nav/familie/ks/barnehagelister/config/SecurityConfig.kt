package no.nav.familie.ks.barnehagelister.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.familie.ks.barnehagelister.rest.ProblemDetailUtils
import no.nav.familie.ks.barnehagelister.security.MaskinportenAuthenticationManager
import no.nav.familie.prosessering.config.ProsesseringInfoProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.springframework.security.web.util.matcher.NegatedRequestMatcher

@Configuration
@EnableWebSecurity
@Profile("!dev")
class SecurityConfig(
    private val maskinportenAuthenticationManager: MaskinportenAuthenticationManager,
) {
    @Bean
    open fun securityFilterChain(
        http: HttpSecurity,
        @Value("\${BARNEHAGELISTER_SCOPE}") scope: String,
    ): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("/internal/**", permitAll)
                authorize("/actuator/**", permitAll)
                authorize("/swagger-ui/**", permitAll)
                authorize("/v3/api-docs/**", permitAll)
                authorize("/swagger-ui.html", permitAll)
                authorize(anyRequest, hasAuthority("SCOPE_$scope"))
            }
            oauth2ResourceServer {
                jwt {
                    authenticationManager = maskinportenAuthenticationManager
                }
                authenticationEntryPoint = problemDetailAuthenticationEntryPoint()
                accessDeniedHandler = problemDetailAccessDeniedHandler()
            }
            csrf { disable() }
        }
        // Forsikre oss om at prosessering-spring-security tar seg av /api/task/**
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

    @Bean
    fun problemDetailAuthenticationEntryPoint(): AuthenticationEntryPoint =
        AuthenticationEntryPoint { _: HttpServletRequest, response: HttpServletResponse, e: AuthenticationException ->
            ProblemDetailUtils.writeProblemDetailResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                e.message ?: "Unauthorized",
                "https://problems-registry.smartbear.com/unauthorized/",
            )
        }

    @Bean
    fun problemDetailAccessDeniedHandler(): AccessDeniedHandler =
        AccessDeniedHandler { _: HttpServletRequest, response: HttpServletResponse, e: AccessDeniedException ->
            ProblemDetailUtils.writeProblemDetailResponse(
                response,
                HttpStatus.FORBIDDEN,
                e.message ?: "Forbidden",
                "https://problems-registry.smartbear.com/forbidden/",
            )
        }

    companion object {
        private val log = LoggerFactory.getLogger(SecurityConfig::class.java)
    }
}
