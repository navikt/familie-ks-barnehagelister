package no.nav.familie.ks.barnehagelister.domene

import no.nav.familie.ks.barnehagelister.kafka.BarnehageBarnKS
import no.nav.familie.ks.barnehagelister.testdata.SkjemaV1TestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SkjemaV1Test {
    @Test
    fun `mapper til BarnehageBarnKS hvis alle felter er utfylt`() {
        // Arrange
        val gyldigSkjemaV1 = SkjemaV1TestData.lagSkjemaV1()

        // Act
        val barnehageBarnKS = gyldigSkjemaV1.mapTilBarnehageBarnKS()!!.first()

        // Assert
        val forventetBarnehageBarnKS = SkjemaV1TestData.lagTilhørendeBarnehageBarnKs()

        assertThat(forventetBarnehageBarnKS).usingRecursiveComparison().ignoringFields("id").isEqualTo(barnehageBarnKS)
    }

    @Test
    fun `mapper til tom liste hvis ingen barnehager`() {
        // Arrange
        val gyldigSkjemaV1 = SkjemaV1TestData.lagSkjemaV1().copy(barnehager = emptyList())

        // Act
        val barnehageBarnKS = gyldigSkjemaV1.mapTilBarnehageBarnKS()

        // Assert
        assertThat(barnehageBarnKS).isEqualTo(emptyList<BarnehageBarnKS>())
    }
}
