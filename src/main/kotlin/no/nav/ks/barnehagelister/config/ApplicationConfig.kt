package no.nav.ks.barnehagelister.config

import no.nav.familie.http.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.http.interceptor.InternLoggerInterceptor
import no.nav.familie.log.filter.LogFilter
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling
import java.time.Duration
import java.time.temporal.ChronoUnit

@SpringBootConfiguration
// @ConfigurationPropertiesScan("no.nav.familie")
// @ComponentScan("no.nav.familie.ks.barnehagelister")
// @EnableJwtTokenValidation(ignore = ["org.springframework", "springfox.documentation.swagger"])
// @EnableOAuth2Client
@Import(
    ConsumerIdClientInterceptor::class,
    InternLoggerInterceptor::class,
//    BearerTokenClientInterceptor::class,
)
@EnableScheduling
class ApplicationConfig {
    @Bean
    fun servletWebServerFactory(): ServletWebServerFactory {
        val serverFactory = JettyServletWebServerFactory()
        serverFactory.port = 8096
        return serverFactory
    }

    @Bean
    fun logFilter(): FilterRegistrationBean<LogFilter> {
        log.info("Registering LogFilter filter")
        val filterRegistration: FilterRegistrationBean<LogFilter> = FilterRegistrationBean()
        filterRegistration.filter = LogFilter()
        filterRegistration.order = 1
        return filterRegistration
    }

    companion object {
        private val log = LoggerFactory.getLogger(ApplicationConfig::class.java)
        private val READ_CONNECTION_LONG_TIMEOUT = Duration.of(10, ChronoUnit.MINUTES)
    }
}
