package no.nav.familie.ks.barnehagelister.domene

import no.nav.familie.ks.barnehagelister.kafka.BarnehagebarnDto
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDate
import java.util.UUID

data class Barnehagebarn(
    @Id
    val id: UUID = UUID.randomUUID(),
    val ident: String,
    val fom: LocalDate,
    val tom: LocalDate? = null,
    val antallTimerIBarnehage: Double,
    val kommuneNavn: String,
    val kommuneNr: String,
    @Column("fk_barnehageliste_id")
    val barnehagelisteId: UUID,
    val organisasjonsnummer: String,
) {
    override fun toString(): String =
        "Barnehagebarn(" +
            "id=$id, " +
            "fom=$fom, " +
            "tom=$tom, " +
            "antallTimerIBarnehage=$antallTimerIBarnehage, " +
            "kommuneNavn='$kommuneNavn', " +
            "kommuneNr='$kommuneNr', " +
            "barnehagelisteId=$barnehagelisteId, " +
            "organisasjonsnummer='$organisasjonsnummer')"
}

fun Barnehagebarn.tilBarnehagebarnDto(): BarnehagebarnDto =
    BarnehagebarnDto(
        id = this.id,
        ident = this.ident,
        fom = this.fom,
        tom = this.tom,
        antallTimerIBarnehage = this.antallTimerIBarnehage,
        kommuneNavn = this.kommuneNavn,
        kommuneNr = this.kommuneNr,
        barnehagelisteId = this.barnehagelisteId,
    )
