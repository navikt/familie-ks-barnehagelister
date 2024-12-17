package no.nav.familie.ks.barnehagelister.domene

data class Barnehage(
    val navn: String,
    val organisasjonsnummer: String,
    val adresse: Adresse?,
    val barnInfolinjer: List<BarnInfolinje>,
)
