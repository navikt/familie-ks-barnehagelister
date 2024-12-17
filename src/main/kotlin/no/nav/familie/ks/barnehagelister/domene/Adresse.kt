package no.nav.familie.ks.barnehagelister.domene

data class Adresse(
    val bruksenhetsnummer: String?,
    val adresselinje1: String?,
    val adresselinje2: String?,
    val postnummer: String,
    val poststed: String,
)
