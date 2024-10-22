package no.nav.familie.ks.barnehagelister.rest

import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
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
    @ApiResponse(
        responseCode = "500",
        description = "Internal Server Error",
        content = [
            Content(
                mediaType = "application/problem+json",
                schema = Schema(implementation = ErrorResponse::class),
            ),
        ],
    )
    fun handleUkjentFeil(
        e: Exception,
        request: HttpServletRequest,
    ): ProblemDetail =
        ProblemDetail
            .forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.message ?: "Ukjent feil")
            .apply {
                type =
                    URI.create(
                        "https://api.swaggerhub.com/domains/smartbear-public/ProblemDetails/1.0.0#/components/responses/ServerError",
                    )

                properties = mapOf("callId" to (MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()))
            }

    @ExceptionHandler(value = [JwtTokenMissingException::class, JwtTokenUnauthorizedException::class])
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = [
            Content(
                mediaType = "application/problem+json",
                schema = Schema(implementation = ErrorResponse::class),
            ),
        ],
    )
    fun onJwtTokenException(
        e: RuntimeException,
        request: HttpServletRequest,
    ): ProblemDetail =
        ProblemDetail
            .forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.message ?: "Unauthorized")
            .apply {
                type =
                    URI.create(
                        "https://api.swaggerhub.com/domains/smartbear-public/ProblemDetails/1.0.0#/components/responses/Unauthorized",
                    )
                properties =
                    mapOf(
                        "callId" to (MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()),
                    )
            }.apply {
                logger.warn("Unauthorized ${this.properties }", e)
            }

    @ExceptionHandler(
        value = [
            HttpMessageNotReadableException::class,
        ],
    )
    @ApiResponse(
        responseCode = "400",
        description = "Bad request",
        content = [
            Content(
                mediaType = "application/problem+json",
                schema = Schema(implementation = ErrorResponse::class),
            ),
        ],
    )
    fun onConstraintViolation(
        e: Exception,
        request: HttpServletRequest,
    ): ProblemDetail =
        ProblemDetail
            .forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message ?: "Bad request")
            .apply {
                type =
                    URI.create(
                        "https://api.swaggerhub.com/domains/smartbear-public/ProblemDetails/1.0.0#/components/responses/Unauthorized",
                    )
                properties = mapOf("callId" to (MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()))
            }
}

class ErrorResponse(
    val status: Int,
    val title: String,
    val detail: String,
    val instance: String,
    val type: URI,
    val callId: String,
)
