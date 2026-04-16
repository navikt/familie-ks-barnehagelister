package no.nav.familie.ks.barnehagelister.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider
import org.springframework.stereotype.Component

@Component
class MaskinportenAuthenticationManager(
    @param:Value("\${BARNEHAGELISTER_SCOPE}") private val scope: String,
    @param:Value("\${MASKINPORTEN_ISSUER}") val issuerUri: String,
) : AuthenticationManager {
    companion object {
        private const val SCOPES_CLAIM = "scope"
    }

    private val delegate: AuthenticationManager by lazy {
        val decoder = JwtDecoders.fromIssuerLocation(issuerUri) as NimbusJwtDecoder
        decoder.setJwtValidator(
            DelegatingOAuth2TokenValidator(
                JwtValidators.createDefaultWithIssuer(issuerUri),
                JwtClaimValidator<List<String>>(SCOPES_CLAIM) { scopesInToken -> scopesInToken.contains(scope) },
            ),
        )
        val provider = JwtAuthenticationProvider(decoder)
        ProviderManager(provider)
    }

    override fun authenticate(authentication: Authentication): Authentication = delegate.authenticate(authentication)
}
