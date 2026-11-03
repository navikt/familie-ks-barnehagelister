package no.nav.familie.ks.barnehagelister.security

import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.OAuth2Config
import no.nav.security.mock.oauth2.token.KeyProvider
import no.nav.security.mock.oauth2.token.OAuth2TokenProvider
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.time.Instant
import java.util.Date

class MaskinportenAuthenticationManagerTest {
    private val nøkkelprovider = KeyProvider()
    private val server = MockOAuth2Server(OAuth2Config(tokenProvider = OAuth2TokenProvider(nøkkelprovider)))
    private val scope = "nav:familie/v1/kontantstotte/barnehagelister"
    private lateinit var autentiseringsmanager: MaskinportenAuthenticationManager

    @BeforeEach
    fun settOpp() {
        server.start()
        autentiseringsmanager = MaskinportenAuthenticationManager(scope, server.issuerUrl("default").toString())
    }

    @AfterEach
    fun ryddOpp() {
        server.shutdown()
    }

    @Test
    fun `skal avvise signert token uten exp`() {
        val opprinneligToken = server.issueToken(claims = mapOf("scope" to scope))
        val claimsUtenUtløp =
            JWTClaimsSet
                .Builder(opprinneligToken.jwtClaimsSet)
                .expirationTime(null)
                .build()
        val tokenUtenUtløp =
            SignedJWT(opprinneligToken.header, claimsUtenUtløp).apply {
                sign(RSASSASigner(nøkkelprovider.signingKey("default").toRSAKey().toPrivateKey()))
            }

        assertThat(SignedJWT.parse(tokenUtenUtløp.serialize()).jwtClaimsSet.claims).doesNotContainKey("exp")

        assertThatThrownBy {
            autentiseringsmanager.authenticate(BearerTokenAuthenticationToken(tokenUtenUtløp.serialize()))
        }.isInstanceOf(InvalidBearerTokenException::class.java)
            .hasMessageContaining("exp is required")
    }

    @Test
    fun `skal godta signert token med gyldig exp`() {
        val token = server.issueToken(claims = mapOf("scope" to scope))

        val autentisering =
            autentiseringsmanager.authenticate(BearerTokenAuthenticationToken(token.serialize())) as JwtAuthenticationToken

        assertThat(autentisering.isAuthenticated).isTrue()
        assertThat(autentisering.token.expiresAt).isNotNull()
        assertThat(autentisering.authorities.map { it.authority }).contains("SCOPE_$scope")
    }

    @Test
    fun `skal avvise signert token når exp finnes og er utløpt`() {
        val utstedelsestidspunkt = Date.from(Instant.now().minusSeconds(7200))
        val utløptToken =
            server.issueToken(
                claims = mapOf("scope" to scope, "iat" to utstedelsestidspunkt, "nbf" to utstedelsestidspunkt),
                expiry = -3600,
            )

        assertThat(utløptToken.jwtClaimsSet.expirationTime).isNotNull()
        assertThatThrownBy {
            autentiseringsmanager.authenticate(BearerTokenAuthenticationToken(utløptToken.serialize()))
        }.isInstanceOf(InvalidBearerTokenException::class.java)
            .hasMessageContaining("expired")
    }
}
