package no.nav.familie.ks.barnehagelister.config

import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.ks.barnehagelister.domene.SkjemaV1
import no.nav.familie.prosessering.PropertiesWrapperTilStringConverter
import no.nav.familie.prosessering.StringTilPropertiesWrapperConverter
import org.postgresql.util.PGobject
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
@EnableJdbcAuditing
@EnableJdbcRepositories("no.nav.familie.ks.barnehagelister", "no.nav.familie.prosessering")
class DatabaseConfig : AbstractJdbcConfiguration() {
    @Bean
    fun transactionManager(dataSource: DataSource): PlatformTransactionManager = DataSourceTransactionManager(dataSource)

    @Bean
    override fun jdbcCustomConversions(): JdbcCustomConversions =
        JdbcCustomConversions(
            listOf(
                PropertiesWrapperTilStringConverter(),
                StringTilPropertiesWrapperConverter(),
                SkjemaPGObjectLesConverter(),
                SkjemaPGOBjectSkrivConverter(),
            ),
        )

    @ReadingConverter
    class SkjemaPGObjectLesConverter : Converter<PGobject, SkjemaV1> {
        override fun convert(pgObject: PGobject): SkjemaV1 = objectMapper.readValue(pgObject.value, SkjemaV1::class.java)
    }

    @WritingConverter
    class SkjemaPGOBjectSkrivConverter : Converter<SkjemaV1, PGobject> {
        override fun convert(skjemaV1: SkjemaV1): PGobject {
            val pgJsonObject = PGobject()
            pgJsonObject.type = "json"
            pgJsonObject.value = objectMapper.writeValueAsString(skjemaV1)
            return pgJsonObject
        }
    }
}
