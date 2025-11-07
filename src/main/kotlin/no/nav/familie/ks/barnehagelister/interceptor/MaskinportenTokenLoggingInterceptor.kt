package no.nav.familie.ks.barnehagelister.interceptor

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.familie.ks.barnehagelister.config.secureLogger
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
        val infoFraToken = request.hentInfoFraToken()

        if (request.requestURI.contains("/ping")) {
            LOG.debug("[pre-handle] $infoFraToken - ping")
            return super.preHandle(request, response, handler)
        }
        LOG.info("[pre-handle] $infoFraToken - ${request.method}: ${request.requestURI}")
        return super.preHandle(request, response, handler)
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        if (request.requestURI.contains("/ping")) {
            // Unngå logging av ping kall for å redusere støy i loggene
            return afterCompletion(request, response, handler, ex)
        }

        val headers = request.hentHeaders()
        secureLogger.info("Request med ${request.requestURI} ${response.status} $headers")

        val infoFraToken = request.hentInfoFraToken()

        val melding = "[post-handle] $infoFraToken - ${request.method}: ${request.requestURI} (${response.status})"
        val orgNo = request.hentSupplierId() ?: request.hentConsumerId() ?: "MANGLER"

        if (HttpStatus.valueOf(response.status).isError) {
            LOG.warn(melding)
        } else {
            LOG.info(melding)
        }

        consumerIdCounters.putIfAbsent(orgNo, Metrics.counter("maskinporten.token.consumer", "id", orgNo))
        consumerIdCounters[orgNo]!!.increment()

        super.afterCompletion(request, response, handler, ex)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(MaskinportenTokenLoggingInterceptor::class.java)
    }
}
