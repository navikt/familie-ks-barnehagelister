package no.nav.familie.ks.barnehagelister

import no.nav.familie.ks.barnehagelister.config.ApplicationConfig
import org.springframework.boot.SpringApplication
import org.springframework.context.annotation.Import
import java.nio.file.Files
import java.nio.file.Path

/**
 * Brukes for å kjøre lokalt med dev-profilen.
 *
 * OBS! Med dev-profilen er det ingen autentisering, og alle endepunkter er åpne. Brukes kun for lokal utvikling.
 * Se DevSecurityConfig for detaljer.
 */
@Import(ApplicationConfig::class)
class DevLauncher

private fun loadDotenvIfPresent(dotenvPath: Path = Path.of(".env")) {
    if (!System.getProperty("AZURE_APP_CLIENT_ID").isNullOrBlank()) return
    if (!Files.exists(dotenvPath)) return

    val line = Files.readAllLines(dotenvPath).firstOrNull { it.startsWith("AZURE_APP_CLIENT_ID=") } ?: return
    val rawValue = line.substringAfter("AZURE_APP_CLIENT_ID=").trim()
    val value = rawValue.removeSurrounding("'").removeSurrounding("\"")

    if (value.isNotBlank()) {
        System.setProperty("AZURE_APP_CLIENT_ID", value)
    }
}

fun main(args: Array<String>) {
    loadDotenvIfPresent()
    System.setProperty("spring.profiles.active", "dev")
    val springApp = SpringApplication(DevLauncher::class.java)
    springApp.setAdditionalProfiles("dev")
    springApp.run(*args)
}
