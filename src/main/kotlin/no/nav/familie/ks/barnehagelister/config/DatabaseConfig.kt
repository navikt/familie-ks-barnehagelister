package no.nav.familie.ks.barnehagelister.config

import no.nav.familie.ks.barnehagelister.rest.dto.FormV1RequestDto
import no.nav.familie.prosessering.PropertiesWrapperTilStringConverter
import no.nav.familie.prosessering.StringTilPropertiesWrapperConverter
import no.nav.familie.restklient.config.jsonMapper
import org.postgresql.util.PGobject
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
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
    override fun userConverters(): List<*> =
        listOf(
            PropertiesWrapperTilStringConverter(),
            StringTilPropertiesWrapperConverter(),
            FormV1RequestDtoPGObjectLesConverter(),
            FormV1RequestDtoPGOBjectSkrivConverter(),
        )

    @ReadingConverter
    class FormV1RequestDtoPGObjectLesConverter : Converter<PGobject, FormV1RequestDto> {
        override fun convert(pgObject: PGobject): FormV1RequestDto = jsonMapper.readValue(pgObject.value, FormV1RequestDto::class.java)
    }

    @WritingConverter
    class FormV1RequestDtoPGOBjectSkrivConverter : Converter<FormV1RequestDto, PGobject> {
        override fun convert(skjemaV1: FormV1RequestDto): PGobject {
            val pgJsonObject = PGobject()
            pgJsonObject.type = "json"
            pgJsonObject.value = jsonMapper.writeValueAsString(skjemaV1)
            return pgJsonObject
        }
    }
}
