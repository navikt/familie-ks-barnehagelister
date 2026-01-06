package no.nav.familie.ks.barnehagelister.rest

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
import tools.jackson.module.kotlin.KotlinInvalidNullException
import java.net.URI

@RestControllerAdvice
class ApiExceptionHandler {
    private val logger: Logger = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    private fun ProblemDetail.toProblemDetailMedCallIdOgErrors(): ProblemDetailMedCallIdOgErrors =
        ProblemDetailMedCallIdOgErrors(hentCallIdFraMDC()).apply {
            status = this@toProblemDetailMedCallIdOgErrors.status
            title = this@toProblemDetailMedCallIdOgErrors.title
            detail = this@toProblemDetailMedCallIdOgErrors.detail
            instance = this@toProblemDetailMedCallIdOgErrors.instance
            type = this@toProblemDetailMedCallIdOgErrors.type
            properties = this@toProblemDetailMedCallIdOgErrors.properties
        }

    @ExceptionHandler(Exception::class)
    fun handleUkjentFeil(
        e: Exception,
        request: HttpServletRequest,
    ): ProblemDetailMedCallIdOgErrors =
        ProblemDetail
            .forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.message ?: "Unknown error")
            .apply {
                type =
                    URI.create(
                        "https://problems-registry.smartbear.com/server-error/",
                    )
            }.toProblemDetailMedCallIdOgErrors()
            .apply {
                logger.error("Ukjent server feil for callId: $callId")
                secureLogger.error("Ukjent server feil for callId: $callId", e)
            }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun skjulNoHandlerFound(
        e: Exception,
        request: HttpServletRequest,
    ): ProblemDetailMedCallIdOgErrors =
        ProblemDetail
            .forStatusAndDetail(HttpStatus.NOT_FOUND, e.message ?: "Not found")
            .apply {
                type =
                    URI.create(
                        "https://problems-registry.smartbear.com/not-found/",
                    )
            }.toProblemDetailMedCallIdOgErrors()
            .apply {
                logger.info("Not-found ${request.method} ${request.requestURI} callId: $callId")
            }

    @ExceptionHandler(value = [JwtTokenMissingException::class, JwtTokenUnauthorizedException::class])
    fun onJwtTokenException(
        e: RuntimeException,
        request: HttpServletRequest,
    ): ProblemDetailMedCallIdOgErrors =
        ProblemDetail
            .forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.message ?: "Unauthorized")
            .apply {
                type =
                    URI.create(
                        "https://problems-registry.smartbear.com/unauthorized/",
                    )
            }.toProblemDetailMedCallIdOgErrors()
            .apply {
                logger.warn("Unauthorized for callId: $callId")
                secureLogger.warn("Unauthorized for callId: $callId", e)
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
    ): ProblemDetailMedCallIdOgErrors {
        val message =
            if (e.cause is KotlinInvalidNullException) {
                val cause = e.cause as KotlinInvalidNullException
                val missingParameter = cause.kotlinPropertyName

                "Couldn't parse request due to missing or null parameter: $missingParameter"
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
            }.toProblemDetailMedCallIdOgErrors()
            .apply {
                when (e) {
                    is JsonValideringsfeilException -> {
                        errors = e.errors
                    }
                }
            }.apply {
                logger.info("ValidationError for callId: $callId, errors: $errors")
                secureLogger.info("ValidationError for callId: $callId, errors: $errors", e)
            }
    }

    @ExceptionHandler(value = [UgyldigKommuneEllerLeverandørFeil::class])
    fun onUgyldigKommuneEllerLeverandørFeil(
        e: Exception,
        request: HttpServletRequest,
    ): ProblemDetailMedCallIdOgErrors =
        ProblemDetail
            .forStatusAndDetail(HttpStatus.FORBIDDEN, e.message ?: "Forbidden")
            .apply {
                type =
                    URI.create(
                        "https://problems-registry.smartbear.com/forbidden/",
                    )
            }.toProblemDetailMedCallIdOgErrors()
            .apply {
                logger.warn("Kalte applikasjonen med en ugyldig kommune eller leverandør. callId: $callId")
                secureLogger.warn("Kalte applikasjonen med en ugyldig kommune eller leverandør. callId: $callId", e)
            }

    private fun hentCallIdFraMDC(): String = MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()

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
