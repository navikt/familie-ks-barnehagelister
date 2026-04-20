package no.nav.familie.ks.barnehagelister.security

import no.nav.familie.ks.barnehagelister.DbContainerInitializer
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev-med-auth")
@ContextConfiguration(initializers = [DbContainerInitializer::class])
class MaskinportenAuthenticationManagerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    companion object {
        private val mockOAuth2Server = MockOAuth2Server()

        @JvmStatic
        @BeforeAll
        fun setup() {
            mockOAuth2Server.start()
        }

        @JvmStatic
        @AfterAll
        fun teardown() {
            mockOAuth2Server.shutdown()
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            val issuerUrl = mockOAuth2Server.issuerUrl("default").toString()
            registry.add("MASKINPORTEN_ISSUER") { issuerUrl }
            registry.add("BARNEHAGELISTER_SCOPE") { "nav:familie/v1/kontantstotte/barnehagelister" }
            registry.add("AZURE_OPENID_CONFIG_ISSUER") { issuerUrl }
            registry.add("AZURE_APP_CLIENT_ID") { "test-client-id" }
            registry.add("AZURE_APP_JWK") { "{}" }
        }
    }

    @Test
    fun `should authenticate successfully with valid token and scope`() {
        val token =
            mockOAuth2Server.issueToken(
                issuerId = "default",
                subject = "test-client",
                claims =
                    mapOf(
                        "scope" to "nav:familie/v1/kontantstotte/barnehagelister other:scope",
                        "client_id" to "test-client-id",
                        "consumer" to mapOf("ID" to "123456789"),
                    ),
            )

        mockMvc
            .perform(
                get("/api/kindergartenlists/ping")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${token.serialize()}"),
            ).andExpect(status().isOk)
    }

    @Test
    fun `should reject token with missing required scope`() {
        val token =
            mockOAuth2Server.issueToken(
                issuerId = "default",
                subject = "test-client",
                claims =
                    mapOf(
                        "scope" to "some:other:scope",
                        "client_id" to "test-client-id",
                    ),
            )

        mockMvc
            .perform(
                get("/api/kindergartenlists/ping")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${token.serialize()}"),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `should reject token with wrong issuer`() {
        val wrongIssuerServer = MockOAuth2Server()
        wrongIssuerServer.start()

        try {
            val token =
                wrongIssuerServer.issueToken(
                    issuerId = "wrong-issuer",
                    subject = "test-client",
                    claims =
                        mapOf(
                            "scope" to "nav:familie/v1/kontantstotte/barnehagelister",
                        ),
                )

            mockMvc
                .perform(
                    get("/api/kindergartenlists/ping")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer ${token.serialize()}"),
                ).andExpect(status().isUnauthorized)
        } finally {
            wrongIssuerServer.shutdown()
        }
    }

    @Test
    fun `should reject expired token`() {
        val token =
            mockOAuth2Server.issueToken(
                issuerId = "default",
                subject = "test-client",
                claims =
                    mapOf(
                        "scope" to "nav:familie/v1/kontantstotte/barnehagelister",
                    ),
                expiry = -3600,
            )

        mockMvc
            .perform(
                get("/api/kindergartenlists/ping")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${token.serialize()}"),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `should reject token with invalid signature`() {
        val token =
            mockOAuth2Server.issueToken(
                issuerId = "default",
                subject = "test-client",
                claims =
                    mapOf(
                        "scope" to "nav:familie/v1/kontantstotte/barnehagelister",
                    ),
            )

        val parts = token.serialize().split(".")
        val tokenWithInvalidSignature = "${parts[0]}.${parts[1]}.invalidsignature"

        mockMvc
            .perform(
                get("/api/kindergartenlists/ping")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $tokenWithInvalidSignature"),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `should handle scope claim with extra whitespace`() {
        val token =
            mockOAuth2Server.issueToken(
                issuerId = "default",
                subject = "test-client",
                claims =
                    mapOf(
                        "scope" to "  nav:familie/v1/kontantstotte/barnehagelister  \t other:scope  ",
                        "client_id" to "test-client-id",
                    ),
            )

        mockMvc
            .perform(
                get("/api/kindergartenlists/ping")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${token.serialize()}"),
            ).andExpect(status().isOk)
    }
}
