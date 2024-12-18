package no.nav.familie.ks.barnehagelister.config

import org.springframework.kafka.annotation.EnableKafka

@EnableKafka
class KafkaConfig {
    companion object {
        const val BARNEHAGELISTE_TOPIC = "ks-barnehageliste"
    }
}
