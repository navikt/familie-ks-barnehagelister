package no.nav.familie.ks.barnehagelister.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * Metrikker for flyten av barnehagelister og barnehagebarn gjennom applikasjonen.
 *
 * Alle metrikker er tellere som eksponeres på /internal/prometheus og er ment for
 * trend- og driftsovervåking i Grafana. De er IKKE en fasit for rapportering, da
 * tellere nullstilles ved deploy og restart. For eksakte tall må databasen brukes.
 *
 * Kardinalitet holdes nede ved at kun `kommuneNr` (4 siffer) og orgnummer uten
 * `0192:`-prefiks brukes som labels. `kommuneNavn` er fritekst og brukes bevisst ikke.
 */
@Component
class BarnehagebarnMetrikker(
    private val meterRegistry: MeterRegistry,
) {
    private val tellere = ConcurrentHashMap<List<String>, Counter>()

    fun tellMottattListe(
        leverandorOrgNr: String,
        resultat: MottattListeResultat,
    ) = teller(
        LISTE_MOTTATT,
        "leverandor_orgnr" to leverandorOrgNr.tilOrgNrLabel(),
        "resultat" to resultat.label,
    ).increment()

    fun tellAvvistListe(
        leverandorOrgNr: String?,
        aarsak: AvvisningsAarsak,
    ) = teller(
        LISTE_AVVIST,
        "leverandor_orgnr" to leverandorOrgNr.tilOrgNrLabel(),
        "aarsak" to aarsak.label,
    ).increment()

    fun tellValideringsfeil(
        type: String,
        antall: Int,
    ) {
        if (antall <= 0) return
        teller(
            LISTE_VALIDERINGSFEIL,
            "type" to type,
        ).increment(antall.toDouble())
    }

    fun tellSendtTilKs(kommuneNr: String) = teller(BARN_SENDT_TIL_KS, "kommune_nr" to kommuneNr).increment()

    private fun teller(
        navn: String,
        vararg tags: Pair<String, String>,
    ): Counter {
        val noekkel = listOf(navn) + tags.flatMap { listOf(it.first, it.second) }
        return tellere.computeIfAbsent(noekkel) {
            Counter
                .builder(navn)
                .tags(tags.map { Tag.of(it.first, it.second) })
                .register(meterRegistry)
        }
    }

    /**
     * Maskinporten oppgir orgnummer på formatet `0192:123456789`. Vi stripper prefikset
     * slik at labelen blir lesbar i Grafana, og faller tilbake til UKJENT når orgnummer
     * ikke er tilgjengelig (f.eks. ved parsefeil før token er lest ut).
     */
    private fun String?.tilOrgNrLabel(): String = this?.substringAfter(":")?.takeIf { it.isNotBlank() } ?: UKJENT

    companion object {
        const val LISTE_MOTTATT = "barnehageliste.mottatt"
        const val LISTE_AVVIST = "barnehageliste.avvist"
        const val LISTE_VALIDERINGSFEIL = "barnehageliste.valideringsfeil"
        const val BARN_SENDT_TIL_KS = "barnehagebarn.sendt.til.ks"

        const val UKJENT = "UKJENT"
    }
}

enum class MottattListeResultat(
    val label: String,
) {
    NY("ny"),
    DUPLIKAT("duplikat"),
}

enum class AvvisningsAarsak(
    val label: String,
) {
    JSON_PARSING("json_parsing"),
    FELT_VALIDERING("felt_validering"),
    UGYLDIG_LEVERANDOR_ELLER_KOMMUNE("ugyldig_leverandor_eller_kommune"),
}
