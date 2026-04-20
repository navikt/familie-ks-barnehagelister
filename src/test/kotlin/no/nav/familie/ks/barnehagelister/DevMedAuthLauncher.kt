package no.nav.familie.ks.barnehagelister

import no.nav.familie.ks.barnehagelister.config.ApplicationConfig
import org.springframework.boot.SpringApplication
import org.springframework.context.annotation.Import
import java.nio.file.Files
import java.nio.file.Path

/**
 * Brukes for å kjøre lokalt med auth (dev-med-auth).
 * - Maskinporten (test.maskinporten.no) for /api/kindergartenlists/ + **
 * - AzureAD for /api/task/ (brukes av familie-prosessering-frontend)
 * OBS! Kafka er fortsatt ikke på med denne profilen
 */
@Import(ApplicationConfig::class)
class DevMedAuthLauncher

fun main(args: Array<String>) {
    lastDotEnvHvisTilstede()
    System.setProperty("spring.profiles.active", "dev-med-auth")
    val springApp = SpringApplication(DevMedAuthLauncher::class.java)
    springApp.setAdditionalProfiles("dev-med-auth")
    springApp.run(*args)
}

private fun lastDotEnvHvisTilstede(dotenvPath: Path = Path.of(".env")) {
    if (!Files.exists(dotenvPath)) return

    val lines = Files.readAllLines(dotenvPath)
    val env =
        lines
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { it.startsWith("#") }
            .mapNotNull { line ->
                val indeksLikhet = line.indexOf('=')
                if (indeksLikhet <= 0) return@mapNotNull null
                val key = line.substring(0, indeksLikhet).trim()
                val raw = line.substring(indeksLikhet + 1).trim()
                val value = raw.removeSurrounding("\"")
                if (key.isBlank() || value.isBlank()) return@mapNotNull null
                key to value
            }.toMap()

    fun setIfMissing(key: String) {
        if (!System.getProperty(key).isNullOrBlank()) return
        val value = env[key] ?: return
        System.setProperty(key, value)
    }

    setIfMissing("AZURE_APP_CLIENT_ID")
    setIfMissing("AZURE_OPENID_CONFIG_ISSUER")
}
