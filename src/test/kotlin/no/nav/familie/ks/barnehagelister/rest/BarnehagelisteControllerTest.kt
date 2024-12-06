package no.nav.familie.ks.barnehagelister.rest

import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.ks.barnehagelister.DbContainerInitializer
import no.nav.familie.ks.barnehagelister.kontrakt.Address
import no.nav.familie.ks.barnehagelister.kontrakt.FormV1
import no.nav.familie.ks.barnehagelister.kontrakt.PersonDTO
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
        val requestBody = BarnehagelisteTestdata.gyldigBarnehageliste().copy(kindergartens = null)
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
    fun `POST barnehageliste - valider at String i listInformation ikke er blanke`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                listInformation =
                    BarnehagelisteTestdata
                        .gyldigBarnehageliste()
                        .listInformation
                        .copy(municipalityNumber = " ", municipalityName = " "),
            )

        val response = sendInvalidBarnehageliste(invalidBarnehageliste)

        val problemDetail = hentProblemDetail(response)

        assertBadRequest(problemDetail)
        assertThat(problemDetail.errors)
            .hasSize(4)
            .contains(
                ValideringsfeilInfo("listInformation.municipalityName", "must not be blank"),
                ValideringsfeilInfo("listInformation.municipalityNumber", "must not be blank"),
            )
    }

    @Test
    fun `POST barnehageliste - valider at at municipalityNumber er 4 siffer`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                listInformation =
                    BarnehagelisteTestdata
                        .gyldigBarnehageliste()
                        .listInformation
                        .copy(municipalityNumber = "0x123", municipalityName = "Oslo"),
            )

        val response = sendInvalidBarnehageliste(invalidBarnehageliste)

        val problemDetail = hentProblemDetail(response)

        assertBadRequest(problemDetail)
        assertThat(problemDetail.errors)
            .hasSize(2)
            .contains(
                ValideringsfeilInfo("listInformation.municipalityNumber", "Municipality number must have 4 digits"),
                ValideringsfeilInfo("listInformation.municipalityNumber", "Municipality number must be a numeric field"),
            )
    }

    @Test
    fun `POST barnehageliste - valider at String i Barnehage ikke er blanke`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                kindergartens =
                    listOf(BarnehagelisteTestdata.lagBarnehage().copy(name = "", organizationNumber = " ")),
            )

        val response = sendInvalidBarnehageliste(invalidBarnehageliste)

        val problemDetail = hentProblemDetail(response)

        assertBadRequest(problemDetail)
        assertThat(problemDetail.errors)
            .hasSize(5)
            .contains(
                ValideringsfeilInfo("kindergartens[0].name", "must not be blank"),
                ValideringsfeilInfo("kindergartens[0].organizationNumber", "must not be blank"),
            )
    }

    @Test
    fun `POST barnehageliste - valider at String i Adresse ikke er blanke`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                kindergartens =
                    listOf(
                        BarnehagelisteTestdata.lagBarnehage().copy(
                            address =
                                Address(
                                    unitNumber = " ",
                                    addressLine1 = " ",
                                    addressLine2 = " ",
                                    zipCode = " ",
                                    postalTown = " ",
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
                ValideringsfeilInfo("kindergartens[0].address.zipCode", "must not be blank"),
                ValideringsfeilInfo("kindergartens[0].address.postalTown", "must not be blank"),
            )
    }

    @Test
    fun `POST barnehageliste - valider at String i Person ikke er blanke`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                kindergartens =
                    listOf(
                        BarnehagelisteTestdata.lagBarnehage().copy(
                            childrenInformation =
                                listOf(
                                    BarnehagelisteTestdata.lagBarninfolinje().copy(
                                        child =
                                            PersonDTO(
                                                firstName = " ",
                                                socialSecurityNumber = " ",
                                                lastName = " ",
                                                address = null,
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
                ValideringsfeilInfo("kindergartens[0].childrenInformation[0].child.lastName", "must not be blank"),
                ValideringsfeilInfo("kindergartens[0].childrenInformation[0].child.socialSecurityNumber", "must not be blank"),
                ValideringsfeilInfo("kindergartens[0].childrenInformation[0].child.firstName", "must not be blank"),
            )
    }

    @Test
    fun `POST barnehageliste - Fødselsnummer må være 11 tegn og numerisk`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                kindergartens =
                    listOf(
                        BarnehagelisteTestdata.lagBarnehage().copy(
                            childrenInformation =
                                listOf(
                                    BarnehagelisteTestdata.lagBarninfolinje().copy(
                                        child =
                                            PersonDTO(
                                                firstName = "Ola Ola",
                                                socialSecurityNumber = "02011212345A",
                                                lastName = "Nordmann",
                                                address = null,
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
                ValideringsfeilInfo(
                    "kindergartens[0].childrenInformation[0].child.socialSecurityNumber",
                    "Social Security Number must be a numeric field",
                ),
                ValideringsfeilInfo(
                    "kindergartens[0].childrenInformation[0].child.socialSecurityNumber",
                    "Social Security Number must have 11 digits",
                ),
            )
    }

    @Test
    fun `POST barnehageliste - valider at organisasjonsnummer er 9 tall`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                kindergartens =
                    listOf(BarnehagelisteTestdata.lagBarnehage().copy(name = "Barnehagenavn", organizationNumber = "03Z1245689")),
            )

        val response = sendInvalidBarnehageliste(invalidBarnehageliste)

        val problemDetail = hentProblemDetail(response)

        assertBadRequest(problemDetail)
        assertThat(problemDetail.errors)
            .hasSize(2)
            .contains(
                ValideringsfeilInfo("kindergartens[0].organizationNumber", "Organization number must be a numeric field"),
                ValideringsfeilInfo("kindergartens[0].organizationNumber", "Organization number must have 9 digits"),
            )
    }

    @Test
    fun `POST barnehageliste - valider at postnummer er 4 numeriske tegn`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                kindergartens =
                    listOf(
                        BarnehagelisteTestdata.lagBarnehage().copy(
                            address =
                                Address(
                                    unitNumber = "H0101",
                                    addressLine1 = "1",
                                    addressLine2 = null,
                                    zipCode = "Z12456",
                                    postalTown = "Oslo",
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
                ValideringsfeilInfo("kindergartens[0].address.zipCode", "Zip code must be a numeric field"),
                ValideringsfeilInfo("kindergartens[0].address.zipCode", "Zip code must have 4 digits"),
            )
    }

    @ParameterizedTest
    @ValueSource(strings = ["H0101", "U1234", "L5423", "K0123"])
    fun `POST barnehageliste - valider gyldig bruksenhetnummer`(bruksenhetnummer: String) {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                kindergartens =
                    listOf(
                        BarnehagelisteTestdata.lagBarnehage().copy(
                            address =
                                Address(
                                    unitNumber = bruksenhetnummer,
                                    addressLine1 = "1",
                                    addressLine2 = null,
                                    zipCode = "0102",
                                    postalTown = "Oslo",
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
    fun `POST barnehageliste - valider at ugylidg bruksenhetnummer gir valideringsfeil`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                kindergartens =
                    listOf(
                        BarnehagelisteTestdata.lagBarnehage().copy(
                            address =
                                Address(
                                    unitNumber = "A056230101",
                                    addressLine1 = "1",
                                    addressLine2 = null,
                                    zipCode = "0102",
                                    postalTown = "Oslo",
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
                ValideringsfeilInfo("kindergartens[0].address.unitNumber", "H, L, U, or K followed by 4 digits"),
                ValideringsfeilInfo("kindergartens[0].address.unitNumber", "Unit number must have 5 characters"),
            )
    }

    @Test
    fun `POST barnehageliste - valider input større enn 200 tegn`() {
        val invalidBarnehageliste =
            BarnehagelisteTestdata.gyldigBarnehageliste().copy(
                listInformation =
                    BarnehagelisteTestdata
                        .gyldigBarnehageliste()
                        .listInformation
                        .copy(municipalityName = "a".repeat(201)),
            )

        val response = sendInvalidBarnehageliste(invalidBarnehageliste)

        val problemDetail = hentProblemDetail(response)

        assertBadRequest(problemDetail)
        assertThat(problemDetail.errors)
            .hasSize(1)
            .contains(
                ValideringsfeilInfo("listInformation.municipalityName", "size must be between 1 and 200"),
            )
    }

    private fun assertBadRequest(problemDetail: ProblemDetailMedCallIdOgErrors) {
        assertThat(problemDetail.type).isEqualTo("https://problems-registry.smartbear.com/validation-error/")
        assertThat(problemDetail.title).isEqualTo("Bad Request")
        assertThat(problemDetail.status).isEqualTo(400)
        assertThat(problemDetail.detail).isEqualTo("Validation error")
        assertThat(problemDetail.callId).isEqualTo("callIdValue")
    }

    private fun sendInvalidBarnehageliste(invalidBarnehageliste: FormV1) =
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
