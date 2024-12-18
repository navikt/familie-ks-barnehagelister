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
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
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
                logger.info("Unauthorized for ${this.properties}")
                secureLogger.error("Unauthorized for ${this.properties}", e)
            }

    @ExceptionHandler(
        value = [
            HttpMessageNotReadableException::class,
            ValideringsfeilException::class,
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

                    is ValideringsfeilException -> {
                        properties =
                            mapOf(
                                "callId" to (MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()),
                                "errors" to e.errors.map { it },
                            )
                    }
                }
            }.apply {
                logger.info("ValidationError for ${this.properties}")
                secureLogger.error("ValidationError for ${this.properties}", e)
            }
    }

    @ExceptionHandler(value = [UkjentLeverandørFeil::class])
    fun onUkjentLeverandørFeil(
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
                logger.info("Kalte applikasjonen med en ukjent leverandør. ${this.properties}")
                secureLogger.error("Kalte applikasjonen med en ukjent leverandør. ${this.properties}", e)
            }
}
