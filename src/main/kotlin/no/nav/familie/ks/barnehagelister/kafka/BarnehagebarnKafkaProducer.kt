package no.nav.familie.ks.barnehagelister.kafka

import no.nav.familie.kontrakter.felles.jsonMapper
import no.nav.familie.ks.barnehagelister.config.KafkaConfig.Companion.BARNEHAGELISTE_TOPIC
import no.nav.familie.ks.barnehagelister.domene.Barnehagebarn
import no.nav.familie.ks.barnehagelister.domene.tilBarnehagebarnDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

interface IBarnehagebarnKafkaProducer {
    fun sendBarnehageBarn(barnehageBarn: Barnehagebarn)
}

@Service
@Profile("preprod", "prod")
class BarnehagebarnKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) : IBarnehagebarnKafkaProducer {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun sendBarnehageBarn(barnehageBarn: Barnehagebarn) {
        val barnehagebarnDto = barnehageBarn.tilBarnehagebarnDto()
        val melding = jsonMapper.writeValueAsString(barnehagebarnDto)

        val logMeldingMetadata =
            "Topicnavn: ${BARNEHAGELISTE_TOPIC} \n" +
                "Nøkkel: ${barnehagebarnDto.id} \n"

        kafkaTemplate
            .send(BARNEHAGELISTE_TOPIC, barnehagebarnDto.id.toString(), melding)
            .thenAccept { logger.info("Melding sendt på kafka. \n" + "Offset: ${it?.recordMetadata?.offset()} \n" + logMeldingMetadata) }
            .exceptionally { throw Exception("Kafkamelding kan ikke sendes. \n" + logMeldingMetadata + "Feilmelding: \"${it.message}\"") }
    }
}

@Service
@Profile("dev")
class DummyBarnehagebarnKafkaProducer : IBarnehagebarnKafkaProducer {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun sendBarnehageBarn(barnehageBarn: Barnehagebarn) {
        logger.info("Ikke enablet kafka. Barnehagebarn som ville blitt lagt på kø: " + barnehageBarn.toString())
    }
}
