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
                            "to Kontanstøtte. The API provides structured endpoint for data retrieval and submission. \n",
                    ).description(
                        """
### Description
The Familie KS Barnehagelister API is designed to process and receive kindergarten lists relevant to Kontantstøtte. The API provides a structured endpoint for submitting kindergarten lists in compliance with [Forskrift om føring av register til bruk i forbindelse med kontroll av beregning og utbetaling av kontantstøtte](https://lovdata.no/dokument/SF/forskrift/2005-12-16-1510?q=kontantst%C3%B8tte). 

### Submission frequency   
Kindergarten lists should be submitted before the 1. of every month. A small period of time is allowed so it is possible to correct the data, the last date for submission for the current month is the 10. of said month. A municipality may have several submissions within the same month to allow for corrections. 

### API Overview
- An endpoint is available for submitting kindergarten lists, supporting multiple kindergartens per request. Each submission must include a unique identifier.
- A separate endpoint is provided for retrieving the submission status. This endpoint will indicate any validation errors that require attention.

### Authentication  
Access to this API requires authentication via a **Maskinporten** token, utilizing the delegation API for Maskinporten. Municipalities must delegate access to their respective suppliers. New suppliers must contact **Nav** to obtain access to the API.  
 
The required token scope:  
```
nav:familie/v1/kontantstotte/barnehagelister
```  
 
Further details are available at: [Maskinporten Delegation API Documentation](https://docs.digdir.no/docs/Maskinporten/maskinporten_func_delegering).  

### Validation errors
The API will return a 400 Bad Request if the JSON is invalid. The response is based on RFC7807 Problem Details for HTTP APIs. 
 
If a required key in the json is missing, the response will be a 400 Bad Request with a JSON body containing the error in the detail field:  
E.g. ```"detail": "Couldn't parse request due to missing or null parameter: name"```
 
If the key value is invalid the detail of the error will be attached to the response:

| Error message                                              | Description                                                                    | Example |
|------------------------------------------------------------|--------------------------------------------------------------------------------| --- |
| Must not be blank                                          | A key in the JSON is blank                                                     | ``` {"parameter": "kindergartens[0].navn", "detail": "must not be blank"}``` |
| Size must be between X and Y                               | A key in the JSON is blank                                                     | ``` {"parameter": "kindergartens[0].navn", "detail": "size must be between 1 and 200"}``` |
| Not a valid organization number 123456789                  | The organization number is not a valid organization number                     | ``` {"parameter": "kindergartens[0].organizationNumber", "detail": "Not a valid organization number 310028141"}``` |
| Social Security Number is not valid                        | The organization number is not a valid organization number                     | ``` {"parameter": "kindergartens[0].childrenInformation[0].child.socialSecurityNumber", "detail": "Social Security Number is not valid"}``` |
| H, L, U, or K followed by 4 digits                         | The unit number for an address is invalid                                      | ``` {"parameter": "kindergartens[0].address.unitNumber", "detail": "H, L, U, or K followed by 4 digits"}``` |
| Zip code must have 4 digits                                | The zip code in the address is not a valid Norwegian zip code                  | ``` {"parameter": "kindergartens[0].address.zipCode", "detail": "Zip code must have 4 digits"}``` |
| A confidential address may not have any address fields set | The JSON has a confidential address set to true, but also contains an adress   | ``` {"parameter": "kindergartens[0].address.addressOrConfidentialAddress", "detail": "A confidential address may not have any address fields set"}``` |
| Mandatory fields zipCode and/or postalTown are not set     | Zip Code and/or postalTown are mandatory for confidential address set to false | ``` {"parameter": "kindergartens[0].childrenInformation[0].child.address.mandatoryFieldsSet", "detail": "Mandatory fields zipCode and/or postalTown are not set"}``` |
 
### Warnings in the status response
When posting the list the API will return a 200 OK if the JSON is valid, but there might be other issues with the data. In those cases the status endpoint will return a JSON body with a list of warnings. 
 
| Warning type | Description |
| --- | --- | 
| OVERLAPPING_PERIOD_WITHIN_SAME_LIST | There are overlapping periods for a child inside the input request |
 
<div id="test-criteria">

### Before going to production
Before you can start using this api in production you need to test at least the following:
 
POST data to https://familie-ks-barnehagelister.intern.dev.nav.no/swagger-ui/index.html#/default-barnehageliste-controller/receiveKindergartenList
whith the example data from swagger and verify that you:
- get a `202 Received and under processing` response.
- get a `200 Done processing` response when you call the endpoint again with the same id.

Run a GET against https://familie-ks-barnehagelister.intern.dev.nav.no/swagger-ui/index.html#/default-barnehageliste-controller/getKindergartenListStatus
whith the `id` of the submitted kindergarten list and verify that you get a `200 Done processing`.
 
</div>
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
