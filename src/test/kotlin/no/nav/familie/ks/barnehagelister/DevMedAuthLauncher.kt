package no.nav.familie.ks.barnehagelister

import no.nav.familie.ks.barnehagelister.config.ApplicationConfig
import org.springframework.boot.SpringApplication
import org.springframework.context.annotation.Import
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Brukes for å kjøre lokalt med auth (dev-med-auth).
 * - Maskinporten (test.maskinporten.no) for /api/kindergartenlists/ + **
 * - AzureAD for /api/task/ (brukes av familie-prosessering-frontend)
 * OBS! Kafka er fortsatt ikke på med denne profilen
 */
@Import(ApplicationConfig::class)
class DevMedAuthLauncher

fun main(args: Array<String>) {
    settClientIdOgSecret()
    System.setProperty("spring.profiles.active", "dev-med-auth")
    val springApp = SpringApplication(DevMedAuthLauncher::class.java)
    springApp.setAdditionalProfiles("dev-med-auth")
    springApp.run(*args)
}

private fun settClientIdOgSecret() {
    val cmd = "src/test/resources/hentMiljøvariabler.sh"

    val process = ProcessBuilder(cmd).start()

    if (process.waitFor() == 1) {
        val inputStream = BufferedReader(InputStreamReader(process.inputStream))
        inputStream.lines().forEach { println(it) }
        inputStream.close()
        throw IllegalStateException("Klarte ikke hente variabler fra Nais. Er du logget på Naisdevice og gcloud?")
    }

    val inputStream = BufferedReader(InputStreamReader(process.inputStream))
    inputStream.readLine() // "Switched to context dev-gcp"
    inputStream
        .readLine()
        .split(";")
        .map { it.split("=") }
        .map { System.setProperty(it[0], it[1]) }
    inputStream.close()
}
