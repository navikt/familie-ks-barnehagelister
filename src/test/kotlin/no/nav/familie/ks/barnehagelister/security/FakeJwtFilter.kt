package no.nav.familie.ks.barnehagelister.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Instant

class FakeJwtFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val jwt =
            Jwt
                .withTokenValue("fake-jwt-token")
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("supplier", mapOf("ID" to "0192:123456789"))
                .claim("consumer", mapOf("ID" to "0192:987654321"))
                .claim("scope", "fake-scope-for-dev-testing")
                .claim("client_id", "dev-test-client")
                .build()

        val authentication =
            JwtAuthenticationToken(
                jwt,
                listOf(SimpleGrantedAuthority("SCOPE_fake-scope-for-dev-testing")),
            )
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication
        SecurityContextHolder.setContext(context)
        filterChain.doFilter(request, response)
    }
}
