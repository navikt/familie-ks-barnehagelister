package no.nav.familie.ks.barnehagelister.interceptor

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

fun hentInfoFraToken(): String {
    val jwtClaims = hentClaims()

    val clientId = jwtClaims?.claims?.get("client_id")
    val scope = jwtClaims?.claims?.get("scope")
    val consumerId = jwtClaims?.hentNestedClaim("consumer")?.get("ID")
    val issuer = jwtClaims?.issuer?.toString()

    val tokenData = "$issuer $clientId $scope $consumerId"
    return tokenData
}

fun hentConsumerId(): String? {
    val jwtClaims = hentClaims()

    val consumerId = jwtClaims?.hentNestedClaim("consumer")?.get("ID")?.toString()
    return consumerId
}

fun hentSupplierId(): String? {
    val jwtClaims = hentClaims()

    val organisasjonsNummer =
        jwtClaims
            ?.hentNestedClaim("supplier")
            ?.get("ID")
            ?.toString()
            ?.substringAfter(":") // ID er på format 0192:<orgno>
    return organisasjonsNummer
}

fun hentClaims(): Jwt? {
    val authentication = SecurityContextHolder.getContext()?.authentication
    return (authentication as? JwtAuthenticationToken)?.token
}

fun Jwt.hentNestedClaim(claim: String): Map<String, Any>? {
    @Suppress("UNCHECKED_CAST")
    return this.claims[claim] as? Map<String, Any>?
}
