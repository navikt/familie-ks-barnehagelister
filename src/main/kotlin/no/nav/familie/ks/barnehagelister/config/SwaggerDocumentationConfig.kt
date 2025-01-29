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
            .info(
                Info()
                    .title("Submission of kindergarten lists for Cash-for-care(Kontantstøtte) benefit")
                    .version("1.0.0")
                    .summary(
                        "The Familie KS Barnehagelister API is designed to handle and receive kindergarten lists relevant " +
                            "to Kontanstøtte applications. The API provides structured endpoint for data retrieval, and submission. \n",
                    ).description(
                        """
                        ### Description  
                        The Familie KS Barnehagelister API is designed to process and receive kindergarten lists relevant to Kontantstøtte applications. The API provides a structured endpoint for submitting kindergarten lists in compliance with [Forskrift om føring av register til bruk i forbindelse med kontroll av beregning og utbetaling av kontantstøtte](https://lovdata.no/dokument/SF/forskrift/2005-12-16-1510?q=kontantst%C3%B8tte).  
                        
                        ### Authentication  
                        Access to this API requires authentication via a **Maskinporten** token, utilizing the delegation API for Maskinporten. Municipalities must delegate access to their respective suppliers. New suppliers must contact **Nav** to obtain access to the API.  
                        
                        The required token scope:  
                        ```
                        nav:familie/v1/kontantstotte/barnehagelister
                        ```  
                        
                        Further details are available at: [Maskinporten Delegation API Documentation](https://docs.digdir.no/docs/Maskinporten/maskinporten_func_delegering).  
                        
                        ### API Overview  
                        - An endpoint is available for submitting kindergarten lists, supporting multiple kindergartens per request. Each submission must include a unique identifier.  
                        - A separate endpoint is provided for retrieving the submission status. This endpoint will indicate any validation errors that require attention.  

                        """.trimIndent(),
                    ),
            ).components(Components().addSecuritySchemes(bearer, bearerTokenSecurityScheme()))
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
