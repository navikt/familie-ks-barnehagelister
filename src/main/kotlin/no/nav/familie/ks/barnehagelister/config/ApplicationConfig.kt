package no.nav.familie.ks.barnehagelister.config

import no.nav.familie.kontrakter.felles.jsonMapperBuilder
import no.nav.familie.ks.barnehagelister.security.AzureJwtAuthenticationConverter
import no.nav.familie.log.NavSystemtype
import no.nav.familie.log.filter.LogFilter
import no.nav.familie.prosessering.config.ProsesseringInfoProvider
import no.nav.familie.restklient.interceptor.ConsumerIdClientInterceptor
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.jetty.servlet.JettyServletWebServerFactory
import org.springframework.boot.web.server.servlet.ServletWebServerFactory
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import tools.jackson.core.StreamReadFeature
import tools.jackson.databind.json.JsonMapper

@SpringBootConfiguration
@ComponentScan("no.nav.familie.ks.barnehagelister", "no.nav.familie.prosessering")
@ConfigurationPropertiesScan("no.nav.familie")
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Import(ConsumerIdClientInterceptor::class)
@EnableScheduling
class ApplicationConfig(
    private val azureJwtAuthenticationConverter: AzureJwtAuthenticationConverter,
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
                    jwtAuthenticationConverter = azureJwtAuthenticationConverter
                }
            }
            csrf { disable() }
        }
        return http.build()
    }

    open fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

       /* configuration.allowedOrigins =
            listOf(
                "https://familie-ks-infotrygd.intern.dev.nav.no",
                "https://familie-ks-infotrygd.intern.nav.no",
                "http://localhost:8080",
            )

        */
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
    fun servletWebServerFactory(): ServletWebServerFactory {
        val serverFactory = JettyServletWebServerFactory()
        serverFactory.port = 8096
        return serverFactory
    }

    @Bean
    fun jsonMapper(): JsonMapper = jsonMapperBuilder.enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build()

    @Bean
    fun logFilter(): FilterRegistrationBean<LogFilter> {
        log.info("Registering LogFilter filter")
        val filterRegistration: FilterRegistrationBean<LogFilter> = FilterRegistrationBean()
        filterRegistration.setFilter(LogFilter(NavSystemtype.NAV_INTEGRASJON))
        filterRegistration.order = 1
        return filterRegistration
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

    companion object {
        private val log = LoggerFactory.getLogger(ApplicationConfig::class.java)
    }
}

val secureLogger = LoggerFactory.getLogger("secureLogger")
