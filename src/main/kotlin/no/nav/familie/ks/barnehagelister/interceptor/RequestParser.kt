package no.nav.familie.ks.barnehagelister.interceptor

import jakarta.servlet.http.HttpServletRequest
import no.nav.security.token.support.core.exceptions.JwtTokenInvalidClaimException
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.core.jwt.JwtTokenClaims

fun HttpServletRequest.hentHeaders() =
    getHeaderNames()?.toList()?.map { headerName ->
        if (headerName == "Authorization") {
            Pair(
                "Authorization",
                getHeader(headerName)?.substring(0, 15),
            )
        } else {
            Pair(headerName, getHeader(headerName))
        }
    }

fun HttpServletRequest.hentInfoFraToken(): String {
    val jwtClaims = hentClaims()

    val clientId = jwtClaims.get("client_id")
    val scope = jwtClaims.get("scope")
    val consumerId = jwtClaims.hentNestedClaim("consumer")?.get("ID")
    val issuer = jwtClaims.issuer

    val tokenData = "$issuer $clientId $scope $consumerId"
    return tokenData
}

fun HttpServletRequest.hentConsumerId(): String? {
    val jwtClaims = hentClaims()

    val consumerId = jwtClaims.hentNestedClaim("consumer")?.get("ID")
    return consumerId
}

fun HttpServletRequest.hentSupplierId(): String? {
    val jwtClaims = hentClaims()

    val organisasjonsNummer =
        jwtClaims
            .hentNestedClaim("supplier")
            ?.get("ID")
            ?.substringAfter(":") // ID er på format 0192:<orgno>
    return organisasjonsNummer
}

fun HttpServletRequest.hentClaims(): JwtTokenClaims {
    val authorizationHeader =
        getHeader("Authorization") ?: throw JwtTokenInvalidClaimException("No Authorization header")
    if (!authorizationHeader.startsWith("Bearer ")) {
        throw JwtTokenInvalidClaimException("Authorization header does not start with 'Bearer '")
    }
    val token = authorizationHeader.substring(7)
    return JwtToken(token).jwtTokenClaims
}

fun JwtTokenClaims.hentNestedClaim(claim: String): Map<String, String>? {
    @Suppress("UNCHECKED_CAST")
    return this.get(claim) as? Map<String, String>?
}
