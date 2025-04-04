package no.nav.familie.ks.barnehagelister.rest

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import jakarta.servlet.http.HttpServletRequest
import no.nav.familie.ks.barnehagelister.config.secureLogger
import no.nav.familie.log.IdUtils
import no.nav.familie.prosessering.util.MDCConstants
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.async.AsyncRequestNotUsableException
import org.springframework.web.servlet.NoHandlerFoundException
import java.net.URI

@RestControllerAdvice
class ApiExceptionHandler {
    private val logger: Logger = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    @ExceptionHandler(Exception::class)
    fun handleUkjentFeil(
        e: Exception,
        request: HttpServletRequest,
    ): ProblemDetail =
        ProblemDetail
            .forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.message ?: "Unknown error")
            .apply {
                type =
                    URI.create(
                        "https://problems-registry.smartbear.com/server-error/",
                    )

                properties = mapOf("callId" to (MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()))
            }.apply {
                logger.error("Ukjent server feil for ${this.properties}")
                secureLogger.error("Ukjent server feil for ${this.properties}", e)
            }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun skjulNoHandlerFound(
        e: Exception,
        request: HttpServletRequest,
    ): ProblemDetail =
        ProblemDetail
            .forStatusAndDetail(HttpStatus.NOT_FOUND, e.message ?: "Not found")
            .apply {
                type =
                    URI.create(
                        "https://problems-registry.smartbear.com/not-found/",
                    )

                properties = mapOf("callId" to (MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()))
            }.apply {
                logger.info("Not-found ${request.method} ${request.requestURI} ${this.properties}")
            }

    @ExceptionHandler(value = [JwtTokenMissingException::class, JwtTokenUnauthorizedException::class])
    fun onJwtTokenException(
        e: RuntimeException,
        request: HttpServletRequest,
    ): ProblemDetail =
        ProblemDetail
            .forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.message ?: "Unauthorized")
            .apply {
                type =
                    URI.create(
                        "https://problems-registry.smartbear.com/unauthorized/",
                    )
                properties =
                    mapOf(
                        "callId" to (MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()),
                    )
            }.apply {
                logger.warn("Unauthorized for ${this.properties}")
                secureLogger.warn("Unauthorized for ${this.properties}", e)
            }

    @ExceptionHandler(
        value = [
            HttpMessageNotReadableException::class,
            JsonValideringsfeilException::class,
        ],
    )
    fun onValideringsFeil(
        e: Exception,
        request: HttpServletRequest,
    ): ProblemDetail {
        val message =
            if (e.cause is MissingKotlinParameterException) {
                val cause = e.cause as MissingKotlinParameterException
                val missingParameter = cause.parameter

                "Couldn't parse request due to missing or null parameter: ${missingParameter.name}"
            } else {
                e.message ?: "Bad request"
            }

        return ProblemDetail
            .forStatusAndDetail(HttpStatus.BAD_REQUEST, message)
            .apply {
                type =
                    URI.create(
                        "https://problems-registry.smartbear.com/validation-error/",
                    )

                when (e) {
                    is HttpMessageNotReadableException -> {
                        properties =
                            mapOf(
                                "callId" to (MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()),
                            )
                    }

                    is JsonValideringsfeilException -> {
                        properties =
                            mapOf(
                                "callId" to (MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()),
                                "errors" to e.errors.map { it },
                            )
                    }
                }
            }.apply {
                logger.info("ValidationError for ${this.properties}")
                secureLogger.info("ValidationError for ${this.properties}", e)
            }
    }

    @ExceptionHandler(value = [UgyldigKommuneEllerLeverandørFeil::class])
    fun onUgyldigKommuneEllerLeverandørFeil(
        e: Exception,
        request: HttpServletRequest,
    ): ProblemDetail =
        ProblemDetail
            .forStatusAndDetail(HttpStatus.FORBIDDEN, e.message ?: "Forbidden")
            .apply {
                type =
                    URI.create(
                        "https://problems-registry.smartbear.com/forbidden/",
                    )
                properties =
                    mapOf(
                        "callId" to (MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()),
                    )
            }.apply {
                logger.warn("Kalte applikasjonen med en ugyldig kommune eller leverandør. ${this.properties}")
                secureLogger.warn("Kalte applikasjonen med en ugyldig kommune eller leverandør. ${this.properties}", e)
            }

    /**
     * AsyncRequestNotUsableException er en exception som blir kastet når en async request blir avbrutt. Velger
     * å skjule denne exceptionen fra loggen da den ikke er interessant for oss.
     */
    @ExceptionHandler(AsyncRequestNotUsableException::class)
    fun handlAsyncRequestNotUsableException(e: AsyncRequestNotUsableException): ResponseEntity<Any> {
        logger.info("En AsyncRequestNotUsableException har oppstått, som skjer når en async request blir avbrutt", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
    }
}
