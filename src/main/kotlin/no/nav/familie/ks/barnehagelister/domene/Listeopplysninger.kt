package no.nav.familie.ks.barnehagelister.domene

import java.time.YearMonth

data class Listeopplysninger(
    val kommunenavn: String,
    val kommunenummer: String,
    val innsendingGjelderArManed: YearMonth,
)
