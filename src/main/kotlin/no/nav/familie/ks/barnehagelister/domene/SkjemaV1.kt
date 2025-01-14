package no.nav.familie.ks.barnehagelister.domene

import no.nav.familie.ks.barnehagelister.kafka.Barnehagebarn
import java.util.UUID

// TODO sanitize JSON input according to OWASP
data class SkjemaV1(
    val id: UUID,
    val barnehager: List<Barnehage>?,
    val listeopplysninger: Listeopplysninger,
)

fun SkjemaV1.mapTilBarnehagebarn(): List<Barnehagebarn> =
    barnehager
        .orEmpty()
        .flatMap { barnehage ->
            barnehage.barnInfolinjer.map { barnInfoLinje ->
                Barnehagebarn(
                    ident = barnInfoLinje.barn.fodselsnummer,
                    fom = barnInfoLinje.startdato,
                    tom = barnInfoLinje.sluttdato,
                    antallTimerIBarnehage = barnInfoLinje.avtaltOppholdstidTimer,
                    kommuneNavn = listeopplysninger.kommunenavn,
                    kommuneNr = listeopplysninger.kommunenummer,
                    barnehagelisteId = id,
                    organisasjonsnummer = barnehage.organisasjonsnummer,
                )
            }
        }
