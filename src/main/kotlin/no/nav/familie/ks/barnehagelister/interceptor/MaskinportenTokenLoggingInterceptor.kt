package no.nav.familie.ks.barnehagelister.interceptor

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.AsyncHandlerInterceptor

@Component
class MaskinportenTokenLoggingInterceptor : AsyncHandlerInterceptor {
    private val consumerIdCounters = mutableMapOf<String, Counter>()

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val infoFraToken = hentInfoFraToken(request)

        LOG.info("[pre-handle] $infoFraToken - ${request.method}: ${request.requestURI}")
        return super.preHandle(request, response, handler)
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        val headers =
            request.getHeaderNames()?.toList()?.map { headerName ->
                if (headerName == "Authorization") {
                    Pair(
                        "Authorization",
                        request.getHeader(headerName)?.substring(0, 15),
                    )
                } else {
                    Pair(headerName, request.getHeader(headerName))
                }
            }
        SECURE_LOG.info("Request med ${request.requestURI } ${response.status} $headers")

        val infoFraToken = hentInfoFraToken(request)

        val melding = "[post-handle] $infoFraToken - ${request.method}: ${request.requestURI} (${response.status})"
        val consumerId = hentConsumerId(request)

        if (HttpStatus.valueOf(response.status).isError) {
            LOG.warn(melding)
        } else {
            LOG.info(melding)
        }

        if (!consumerIdCounters.containsKey(consumerId)) {
            consumerIdCounters[consumerId] = Metrics.counter("maskinporten.token.consumer", "id", consumerId)
        }
        consumerIdCounters[consumerId]!!.increment()

        super.afterCompletion(request, response, handler, ex)
    }

    private fun hentInfoFraToken(request: HttpServletRequest): String {
        val jwtClaims = hentClaims(request)

        val clientId = jwtClaims?.get("client_id")
        val scope = jwtClaims?.get("scope")
        val consumerId = (jwtClaims?.get("consumer") as? Map<String, String>)?.get("ID")
        val issuer = jwtClaims?.issuer

        val tokenData = "$issuer $clientId $scope $consumerId"
        return tokenData
    }

    private fun hentConsumerId(request: HttpServletRequest): String {
        val jwtClaims = hentClaims(request)

        return if ((
                jwtClaims?.get(
                    "consumer",
                ) as? Map<String, String>
            )?.get(
                "ID",
            ) == null
        ) {
            "MANGLER"
        } else {
            (jwtClaims?.get("consumer") as? Map<String, String>)?.get("ID").toString()
        }
    }

    private fun hentClaims(request: HttpServletRequest): JwtTokenClaims? {
        val authorizationHeader = request.getHeader("Authorization")
        val token =
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                authorizationHeader.substring(7)
            } else {
                null
            }

        if (token == null) {
            return null
        } else {
            return JwtToken(token).jwtTokenClaims
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(MaskinportenTokenLoggingInterceptor::class.java)
        private val SECURE_LOG = LoggerFactory.getLogger("secureLogger")
    }
}
