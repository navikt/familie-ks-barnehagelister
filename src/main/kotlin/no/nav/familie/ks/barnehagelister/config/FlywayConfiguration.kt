package no.nav.familie.ks.barnehagelister.config

import org.flywaydb.core.api.configuration.FluentConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile

@Profile("!dev")
@ConditionalOnProperty("spring.flyway.enabled")
data class FlywayConfiguration(
    private val role: String,
) {
    @Bean
    fun flywayConfig(): FlywayConfigurationCustomizer =
        FlywayConfigurationCustomizer { c: FluentConfiguration ->
            c.initSql("SET ROLE \"$role\"")
        }
}
