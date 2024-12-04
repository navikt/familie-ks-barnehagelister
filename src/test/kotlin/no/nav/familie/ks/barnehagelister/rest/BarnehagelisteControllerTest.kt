package no.nav.familie.ks.barnehagelister.rest

import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.ks.barnehagelister.DbContainerInitializer
import no.nav.familie.ks.barnehagelister.kontrakt.Adresse
import no.nav.familie.ks.barnehagelister.kontrakt.Person
import no.nav.familie.ks.barnehagelister.kontrakt.SkjemaV1
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.Charset

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension::class)
@ActiveProfiles("dev")
@ContextConfiguration(initializers = [DbContainerInitializer::class])
@EnableMockOAuth2Server
class BarnehagelisteControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `GET ping skal returnere 200 OK`() {
        this.mockMvc
            .perform(get("/api/barnehagelister/ping"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(content().json("\"OK\""))
    }

    @Test
    fun `POST gyldig barnehagelister skal returnerere 202 OK`() {
        val requestBody = BarnehagelisteTestdata.gyldigBarnehageliste()
        this.mockMvc
            .perform(
                post("/api/barnehagelister/v1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)),
            ).andExpect(status().isAccepted)
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("id").value(requestBody.id.toString()))
            .andExpect(jsonPath("status").value("RECEIVED"))
            .andExpect(jsonPath("mottattTid").isNotEmpty)
            .andExpect(jsonPath("ferdigTid").value(null))
            .andExpect(jsonPath("links.status").value("/api/barnehagelister/status/${requestBody.id}"))
    }

    @Test
    fun `POST gyldig barnehagelister uten barnehager skal returnere 202 OK`() {
        val requestBody = BarnehagelisteTestdata.gyldigBarnehageliste().copy(barnehager = null)
        this.mockMvc
            .perform(
                post("/api/barnehagelister/v1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)),
            )
            // TODO I dette tilfellet kan vi likegodt returnere 200 OK, da vi ikke har noen barnehager å prosessere
            .andExpect(status().isAccepted)
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("id").value(requestBody.id.toString()))
            .andExpect(jsonPath("status").value("RECEIVED"))
            .andExpect(jsonPath("mottattTid").isNotEmpty)
            .andExpect(jsonPath("ferdigTid").value(null))
            .andExpect(jsonPath("links.status").value("/api/barnehagelister/status/${requestBody.id}"))
    }

    @Test
    fun `POST barnehageliste - valider at String i listeopplysninger ikke er blanke`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                listeopplysninger =
                    BarnehagelisteTestdata
                        .gyldigBarnehageliste()
                        .listeopplysninger
                        .copy(kommunenummer = " ", kommunenavn = " "),
            )

        val response = sendInvalidBarnehageliste(invalidBarnehageliste)

        val problemDetail = hentProblemDetail(response)

        assertBadRequest(problemDetail)
        assertThat(problemDetail.errors)
            .hasSize(4)
            .contains(
                ValideringsfeilInfo("listeopplysninger.kommunenavn", "must not be blank"),
                ValideringsfeilInfo("listeopplysninger.kommunenummer", "must not be blank"),
            )
    }

    @Test
    fun `POST barnehageliste - valider at at kommunenummer er 4 siffer`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                listeopplysninger =
                    BarnehagelisteTestdata
                        .gyldigBarnehageliste()
                        .listeopplysninger
                        .copy(kommunenummer = "0x123", kommunenavn = "Oslo"),
            )

        val response = sendInvalidBarnehageliste(invalidBarnehageliste)

        val problemDetail = hentProblemDetail(response)

        assertBadRequest(problemDetail)
        assertThat(problemDetail.errors)
            .hasSize(2)
            .contains(
                ValideringsfeilInfo("listeopplysninger.kommunenummer", "Municipality number must have 4 digits"),
                ValideringsfeilInfo("listeopplysninger.kommunenummer", "Municipality number must be a numeric field"),
            )
    }

    @Test
    fun `POST barnehageliste - valider at String i Barnehage ikke er blanke`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                barnehager =
                    listOf(BarnehagelisteTestdata.lagBarnehage().copy(navn = "", organisasjonsnummer = " ")),
            )

        val response = sendInvalidBarnehageliste(invalidBarnehageliste)

        val problemDetail = hentProblemDetail(response)

        assertBadRequest(problemDetail)
        assertThat(problemDetail.errors)
            .hasSize(5)
            .contains(
                ValideringsfeilInfo("barnehager[0].navn", "must not be blank"),
                ValideringsfeilInfo("barnehager[0].organisasjonsnummer", "must not be blank"),
            )
    }

    @Test
    fun `POST barnehageliste - valider at String i Adresse ikke er blanke`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                barnehager =
                    listOf(
                        BarnehagelisteTestdata.lagBarnehage().copy(
                            adresse =
                                Adresse(
                                    bruksenhetsnummer = " ",
                                    adresselinje1 = " ",
                                    adresselinje2 = " ",
                                    postnummer = " ",
                                    poststed = " ",
                                ),
                        ),
                    ),
            )

        val response = sendInvalidBarnehageliste(invalidBarnehageliste)

        val problemDetail = hentProblemDetail(response)

        assertBadRequest(problemDetail)
        assertThat(problemDetail.errors)
            .hasSize(6)
            .contains(
                ValideringsfeilInfo("barnehager[0].adresse.postnummer", "must not be blank"),
                ValideringsfeilInfo("barnehager[0].adresse.poststed", "must not be blank"),
            )
    }

    @Test
    fun `POST barnehageliste - valider at String i Person ikke er blanke`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                barnehager =
                    listOf(
                        BarnehagelisteTestdata.lagBarnehage().copy(
                            barnInfolinjer =
                                listOf(
                                    BarnehagelisteTestdata.lagBarninfolinje().copy(
                                        barn =
                                            Person(
                                                fornavn = " ",
                                                fodselsnummer = " ",
                                                etternavn = " ",
                                                adresse = null,
                                            ),
                                    ),
                                ),
                        ),
                    ),
            )

        val response = sendInvalidBarnehageliste(invalidBarnehageliste)

        val problemDetail = hentProblemDetail(response)

        assertBadRequest(problemDetail)
        assertThat(problemDetail.errors)
            .hasSize(5)
            .contains(
                ValideringsfeilInfo("barnehager[0].barnInfolinjer[0].barn.etternavn", "must not be blank"),
                ValideringsfeilInfo("barnehager[0].barnInfolinjer[0].barn.fodselsnummer", "must not be blank"),
                ValideringsfeilInfo("barnehager[0].barnInfolinjer[0].barn.fornavn", "must not be blank"),
            )
    }

    @Test
    fun `POST barnehageliste - Fødselsnummer må være 11 tegn og numerisk`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                barnehager =
                    listOf(
                        BarnehagelisteTestdata.lagBarnehage().copy(
                            barnInfolinjer =
                                listOf(
                                    BarnehagelisteTestdata.lagBarninfolinje().copy(
                                        barn =
                                            Person(
                                                fornavn = "Ola Ola",
                                                fodselsnummer = "02011212345A",
                                                etternavn = "Nordmann",
                                                adresse = null,
                                            ),
                                    ),
                                ),
                        ),
                    ),
            )

        val response = sendInvalidBarnehageliste(invalidBarnehageliste)

        val problemDetail = hentProblemDetail(response)

        assertBadRequest(problemDetail)
        assertThat(problemDetail.errors)
            .hasSize(2)
            .contains(
                ValideringsfeilInfo("barnehager[0].barnInfolinjer[0].barn.fodselsnummer", "Social Security Number must be a numeric field"),
                ValideringsfeilInfo("barnehager[0].barnInfolinjer[0].barn.fodselsnummer", "Social Security Number must have 11 digits"),
            )
    }

    @Test
    fun `POST barnehageliste - valider at organisasjonsnummer er 9 tall`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                barnehager =
                    listOf(BarnehagelisteTestdata.lagBarnehage().copy(navn = "Barnehagenavn", organisasjonsnummer = "03Z1245689")),
            )

        val response = sendInvalidBarnehageliste(invalidBarnehageliste)

        val problemDetail = hentProblemDetail(response)

        assertBadRequest(problemDetail)
        assertThat(problemDetail.errors)
            .hasSize(2)
            .contains(
                ValideringsfeilInfo("barnehager[0].organisasjonsnummer", "Organization number must be a numeric field"),
                ValideringsfeilInfo("barnehager[0].organisasjonsnummer", "Organization number must have 9 digits"),
            )
    }

    @Test
    fun `POST barnehageliste - valider at postnummer er 4 numeriske tegn`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                barnehager =
                    listOf(
                        BarnehagelisteTestdata.lagBarnehage().copy(
                            adresse =
                                Adresse(
                                    bruksenhetsnummer = "H0101",
                                    adresselinje1 = "1",
                                    adresselinje2 = null,
                                    postnummer = "Z12456",
                                    poststed = "Oslo",
                                ),
                        ),
                    ),
            )

        val response = sendInvalidBarnehageliste(invalidBarnehageliste)

        val problemDetail = hentProblemDetail(response)

        assertBadRequest(problemDetail)
        assertThat(problemDetail.errors)
            .hasSize(2)
            .contains(
                ValideringsfeilInfo("barnehager[0].adresse.postnummer", "Postal code must be a numeric field"),
                ValideringsfeilInfo("barnehager[0].adresse.postnummer", "Postal code must have 4 digits"),
            )
    }

    @ParameterizedTest
    @ValueSource(strings = ["H0101", "U1234", "L5423", "K0123"])
    fun `POST barnehageliste - valider gyldig bruksnummer`(bruksnummer: String) {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                barnehager =
                    listOf(
                        BarnehagelisteTestdata.lagBarnehage().copy(
                            adresse =
                                Adresse(
                                    bruksenhetsnummer = bruksnummer,
                                    adresselinje1 = "1",
                                    adresselinje2 = null,
                                    postnummer = "0102",
                                    poststed = "Oslo",
                                ),
                        ),
                    ),
            )

        this.mockMvc
            .perform(
                post("/api/barnehagelister/v1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Correlation-Id", "callIdValue")
                    .content(objectMapper.writeValueAsString(invalidBarnehageliste)),
            ).andExpect(status().isAccepted)
    }

    @Test
    fun `POST barnehageliste - valider at ugylidg bruksnummer gir valideringsfeil`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                barnehager =
                    listOf(
                        BarnehagelisteTestdata.lagBarnehage().copy(
                            adresse =
                                Adresse(
                                    bruksenhetsnummer = "A056230101",
                                    adresselinje1 = "1",
                                    adresselinje2 = null,
                                    postnummer = "0102",
                                    poststed = "Oslo",
                                ),
                        ),
                    ),
            )

        val response = sendInvalidBarnehageliste(invalidBarnehageliste)

        val problemDetail = hentProblemDetail(response)

        assertBadRequest(problemDetail)
        assertThat(problemDetail.errors)
            .hasSize(2)
            .contains(
                ValideringsfeilInfo("barnehager[0].adresse.bruksenhetsnummer", "H, L, U, or K followed by 4 digits"),
                ValideringsfeilInfo("barnehager[0].adresse.bruksenhetsnummer", "Unit number must have 5 characters"),
            )
    }

    @Test
    fun `POST barnehageliste - valider input større enn 200 tegn`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                listeopplysninger =
                    BarnehagelisteTestdata
                        .gyldigBarnehageliste()
                        .listeopplysninger
                        .copy(kommunenavn = "a".repeat(201)),
            )

        val response = sendInvalidBarnehageliste(invalidBarnehageliste)

        val problemDetail = hentProblemDetail(response)

        assertBadRequest(problemDetail)
        assertThat(problemDetail.errors)
            .hasSize(1)
            .contains(
                ValideringsfeilInfo("listeopplysninger.kommunenavn", "size must be between 1 and 200"),
            )
    }

    private fun assertBadRequest(problemDetail: ProblemDetailMedCallIdOgErrors) {
        assertThat(problemDetail.type).isEqualTo("https://problems-registry.smartbear.com/validation-error/")
        assertThat(problemDetail.title).isEqualTo("Bad Request")
        assertThat(problemDetail.status).isEqualTo(400)
        assertThat(problemDetail.detail).isEqualTo("Validation error")
        assertThat(problemDetail.callId).isEqualTo("callIdValue")
    }

    private fun sendInvalidBarnehageliste(invalidBarnehageliste: SkjemaV1) =
        this.mockMvc
            .perform(
                post("/api/barnehagelister/v1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Correlation-Id", "callIdValue")
                    .content(objectMapper.writeValueAsString(invalidBarnehageliste)),
            ).andExpect(status().isBadRequest)
            .andExpect(content().contentType("application/problem+json"))
            .andReturn()

    private fun hentProblemDetail(response: MvcResult): ProblemDetailMedCallIdOgErrors =
        objectMapper
            .readValue(
                response.response.getContentAsString(Charset.forName("UTF-8")),
                ProblemDetailMedCallIdOgErrors::class.java,
            )
}
