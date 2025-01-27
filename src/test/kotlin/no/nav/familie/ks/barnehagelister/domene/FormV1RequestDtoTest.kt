package no.nav.familie.ks.barnehagelister.domene

import no.nav.familie.ks.barnehagelister.rest.dto.mapTilBarnehagebarn
import no.nav.familie.ks.barnehagelister.testdata.FormV1RequestDtoTestData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FormV1RequestDtoTest {
    @Test
    fun `mapper til BarnehageBarnKS hvis alle felter er utfylt`() {
        // Arrange
        val gyldigRequest = FormV1RequestDtoTestData.lagRequest()

        // Act
        val barnehageBarnKS = gyldigRequest.mapTilBarnehagebarn()!!.first()

        // Assert
        val forventetBarnehageBarnKS = FormV1RequestDtoTestData.lagTilhørendeBarnehagebarn()

        assertThat(forventetBarnehageBarnKS).usingRecursiveComparison().ignoringFields("id").isEqualTo(barnehageBarnKS)
    }

    @Test
    fun `mapper til tom liste hvis ingen barnehager`() {
        // Arrange
        val gyldigRequest = FormV1RequestDtoTestData.lagRequest().copy(kindergartens = emptyList())

        // Act
        val barnehageBarnKS = gyldigRequest.mapTilBarnehagebarn()

        // Assert
        assertThat(barnehageBarnKS).isEqualTo(emptyList<Barnehagebarn>())
    }
}
