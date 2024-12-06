package no.nav.familie.ks.barnehagelister.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerDocumentationConfig {
    private val bearer = "Bearer"

    @Bean
    fun openApi(): OpenAPI =
        OpenAPI()
            .info(Info().title("Submission of kindergarten lists for Kontantstøtte").version("1.0.0"))
            .components(Components().addSecuritySchemes(bearer, bearerTokenSecurityScheme()))
            .addSecurityItem(SecurityRequirement().addList(bearer, listOf("read", "write")))

    @Bean
    fun customOpenApi(): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group("default")
            .packagesToScan("no.nav.familie.ks.barnehagelister.rest")
            .build()

    private fun bearerTokenSecurityScheme(): SecurityScheme =
        SecurityScheme()
            .type(SecurityScheme.Type.APIKEY)
            .scheme(bearer)
            .bearerFormat("JWT")
            .`in`(SecurityScheme.In.HEADER)
            .name("Authorization")
}
