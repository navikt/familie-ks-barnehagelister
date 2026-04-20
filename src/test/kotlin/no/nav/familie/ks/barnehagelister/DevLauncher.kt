package no.nav.familie.ks.barnehagelister

import no.nav.familie.ks.barnehagelister.config.ApplicationConfig
import org.springframework.boot.SpringApplication
import org.springframework.context.annotation.Import

/**
 * Brukes for å kjøre lokalt med dev-profilen.
 *
 * OBS! Med dev-profilen er det ingen autentisering, og alle endepunkter er åpne. Brukes kun for lokal utvikling.
 */
@Import(ApplicationConfig::class)
class DevLauncher

fun main(args: Array<String>) {
    System.setProperty("spring.profiles.active", "dev")
    val springApp = SpringApplication(DevLauncher::class.java)
    springApp.setAdditionalProfiles("dev")
    springApp.run(*args)
}
