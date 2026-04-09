package no.nav.familie.ks.barnehagelister.security

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component
class MaskinportenJwtAuthenticationConverter(
    @param:Value("\${BARNEHAGELISTER_SCOPE}") private val scope: String,
) : Converter<Jwt, AbstractAuthenticationToken> {
    private val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")

    companion object {
        private const val SCOPES_CLAIM = "scope"
    }

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val scopes = jwt.getClaimAsStringList(SCOPES_CLAIM) ?: emptyList()

        if (!scopes.contains(scope)) {
            secureLogger.warn(
                "Token mangler riktig scope. Scopes i token: ${scopes.joinToString(", ")}",
            )
        }

        return JwtAuthenticationToken(jwt, listOf(SimpleGrantedAuthority("SCOPE_$scope")))
    }
}
