package no.nav.familie.ba.skatteetaten.maskinporten

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.time.Instant
import java.util.Date

fun main() {
    val jwkPrivate: String = System.getenv("MASKINPORTEN_CLIENT_JWK")
    assert(jwkPrivate.isNotBlank())
    val clientId: String = System.getenv("MASKINPORTEN_CLIENT_ID")
    assert(clientId.isNotBlank())

    val token = MaskinportenClient().hentToken("nav:familie/v1/kontantstotte/barnehagelister", jwkPrivate, clientId)

    println(token)
}

class MaskinportenClient {
    private val restTemplate = RestTemplate()

    private fun createSignedJWT(
        rsaJwk: RSAKey,
        claimsSet: JWTClaimsSet?,
    ): SignedJWT =
        try {
            val header =
                JWSHeader
                    .Builder(JWSAlgorithm.RS256)
                    .keyID(rsaJwk.keyID)
                    .type(JOSEObjectType.JWT)
            val signedJWT = SignedJWT(header.build(), claimsSet)
            val signer: JWSSigner = RSASSASigner(rsaJwk.toPrivateKey())
            signedJWT.sign(signer)
            signedJWT
        } catch (e: JOSEException) {
            throw RuntimeException(e)
        }

    fun hentToken(
        scope: String,
        jwkPrivate: String,
        clientId: String,
    ): String {
        val rsaKey = RSAKey.parse(jwkPrivate)
        val time = Instant.now()
        val jwtClaimsSet =
            JWTClaimsSet
                .Builder()
                .audience(AUD)
                .issuer(clientId)
                .issueTime(Date.from(time))
                .expirationTime(Date.from(time.plusSeconds(120)))
                .claim(SCOPE, scope)
                .build()
        val signedJWT = createSignedJWT(rsaKey, jwtClaimsSet)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val requestBody = LinkedMultiValueMap<Any, Any>()
        requestBody.add("grant_type", GRANT_TYPE_VALUE)
        requestBody.add("assertion", signedJWT.serialize())
        val httpEntity = HttpEntity(requestBody, headers)
        val json =
            restTemplate
                .exchange(
                    TOKEN_ENDPOINT,
                    HttpMethod.POST,
                    httpEntity,
                    String::class.java,
                ).body!!

        return json
    }

    companion object {
        private const val SCOPE = "scope"
        private const val GRANT_TYPE_VALUE = "urn:ietf:params:oauth:grant-type:jwt-bearer"
        private const val AUD = "https://test.maskinporten.no/"
        private const val TOKEN_ENDPOINT = "$AUD/token"
    }
}
