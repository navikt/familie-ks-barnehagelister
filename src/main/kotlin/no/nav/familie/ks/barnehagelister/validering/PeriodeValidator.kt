package no.nav.familie.ks.barnehagelister.validering

import no.nav.familie.ks.barnehagelister.domene.Barnehagebarn
import java.time.LocalDate

fun List<Barnehagebarn>.validerIngenOverlapp(feilmelding: String = "Feil med tidslinje. Overlapp på periode") {
    this
        .sortedBy { it.fom }
        .map { Pair(it.fom, it.tom?.plusDays(1) ?: LocalDate.MAX) }
        .zipWithNext { a, b ->
            if (a.second.isAfter(b.first)) {
                error(message = feilmelding)
            }
        }
}
