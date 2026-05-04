package no.nav.familie.ks.barnehagelister.config

import no.nav.familie.kontrakter.felles.jsonMapperBuilder
import no.nav.familie.log.NavSystemtype
import no.nav.familie.log.filter.LogFilter
import no.nav.familie.restklient.interceptor.ConsumerIdClientInterceptor
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.jetty.servlet.JettyServletWebServerFactory
import org.springframework.boot.web.server.servlet.ServletWebServerFactory
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling
import tools.jackson.core.StreamReadFeature
import tools.jackson.databind.json.JsonMapper

@SpringBootConfiguration
@ComponentScan("no.nav.familie.ks.barnehagelister", "no.nav.familie.prosessering")
@ConfigurationPropertiesScan("no.nav.familie")
@Import(ConsumerIdClientInterceptor::class)
@EnableScheduling
class ApplicationConfig {
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

    companion object {
        private val log = LoggerFactory.getLogger(ApplicationConfig::class.java)
    }
}

val secureLogger = LoggerFactory.getLogger("secureLogger")
