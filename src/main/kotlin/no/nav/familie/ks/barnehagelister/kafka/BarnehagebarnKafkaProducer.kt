package no.nav.familie.ks.barnehagelister.kafka

import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.ks.barnehagelister.config.KafkaConfig.Companion.BARNEHAGELISTE_TOPIC
import no.nav.familie.ks.barnehagelister.rest.ApiExceptionHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class BarnehagebarnKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) {
    private val logger: Logger = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    fun sendBarnehageBarn(barnehageBarn: BarnehageBarnKS) {
        val melding = objectMapper.writeValueAsString(barnehageBarn)

        val logMeldingMetadata =
            "Topicnavn: ${BARNEHAGELISTE_TOPIC} \n" +
                "Nøkkel: ${barnehageBarn.id} \n"

        kafkaTemplate
            .send(BARNEHAGELISTE_TOPIC, barnehageBarn.id.toString(), melding)
            .thenAccept { logger.info("Melding sendt på kafka. \n" + "Offset: ${it?.recordMetadata?.offset()} \n" + logMeldingMetadata) }
            .exceptionally { throw Exception("Kafkamelding kan ikke sendes. \n" + logMeldingMetadata + "Feilmelding: \"${it.message}\"") }
    }
}
