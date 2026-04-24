package no.nav.familie.ks.barnehagelister.security

import no.nav.familie.prosessering.config.ProsesseringInfoProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.context.SecurityContextHolderFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("dev")
class DevSecurityConfig {
    @Bean
    open fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize(anyRequest, permitAll)
            }
            csrf { disable() }
        }
        http.addFilterAfter(FakeJwtFilter(), SecurityContextHolderFilter::class.java)

        return http.build()
    }

    @Bean
    fun prosesseringInfoProvider() =
        object : ProsesseringInfoProvider {
            override fun hentBrukernavn(): String = "VL"

            override fun harTilgang(): Boolean = true
        }
}
