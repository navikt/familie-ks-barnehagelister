package no.nav.familie.ks.barnehagelister.rest

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import no.nav.familie.log.IdUtils
import no.nav.familie.prosessering.util.MDCConstants
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import java.net.URI

object ProblemDetailUtils {
    private val objectMapper: ObjectMapper = ObjectMapper().findAndRegisterModules()

    fun writeProblemDetailResponse(
        response: HttpServletResponse,
        status: HttpStatus,
        detail: String,
        typeUri: String,
    ) {
        val problemDetail =
            ProblemDetail.forStatusAndDetail(status, detail).apply {
                type = URI.create(typeUri)
            }

        val problemDetailMedCallIdOgErrors =
            problemDetail.toProblemDetailMedCallIdOgErrors()

        response.status = status.value()
        response.contentType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
        objectMapper.writeValue(response.writer, problemDetailMedCallIdOgErrors)
    }

    fun ProblemDetail.toProblemDetailMedCallIdOgErrors(): ProblemDetailMedCallIdOgErrors =
        ProblemDetailMedCallIdOgErrors(hentCallIdFraMDC()).apply {
            status = this@toProblemDetailMedCallIdOgErrors.status
            title = this@toProblemDetailMedCallIdOgErrors.title
            detail = this@toProblemDetailMedCallIdOgErrors.detail
            instance = this@toProblemDetailMedCallIdOgErrors.instance
            type = this@toProblemDetailMedCallIdOgErrors.type
            properties = this@toProblemDetailMedCallIdOgErrors.properties
        }

    private fun hentCallIdFraMDC(): String = MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()
}
