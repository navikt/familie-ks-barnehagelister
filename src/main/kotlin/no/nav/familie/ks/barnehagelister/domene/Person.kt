package no.nav.familie.ks.barnehagelister.domene

data class Person(
    val fodselsnummer: String,
    val fornavn: String,
    val etternavn: String,
    val adresse: Adresse?,
)
