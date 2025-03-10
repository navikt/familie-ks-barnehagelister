package no.nav.familie.ks.barnehagelister

import no.nav.familie.ks.barnehagelister.config.ApplicationConfig
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.springframework.boot.SpringApplication
import org.springframework.context.annotation.Import

@Import(ApplicationConfig::class)
@EnableMockOAuth2Server
class DevLauncher

fun main(args: Array<String>) {
    System.setProperty("spring.profiles.active", "dev")
    val springApp = SpringApplication(DevLauncher::class.java)
    springApp.setAdditionalProfiles("dev")
    springApp.run(*args)
}
