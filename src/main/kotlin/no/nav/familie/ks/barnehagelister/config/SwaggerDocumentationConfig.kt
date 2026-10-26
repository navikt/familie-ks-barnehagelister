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
The Familie KS Barnehagelister API is designed to process and receive kindergarten lists relevant to Kontantstøtte.  

### Regulation
[Forskrift om føring av register til bruk i forbindelse med kontroll av beregning og utbetaling av kontantstøtte](https://lovdata.no/dokument/SF/forskrift/2005-12-16-1510?q=kontantst%C3%B8tte).

The API provides a structured endpoint for submitting kindergarten lists in compliance with the regulation. The regulation
contains information such as
  * Requirements for the municipality
  * What information is required to be sent in, which children should be included, etc.
  * Frequency of submission
  * Retention of data
  * Rights of data access for parents and children
  
### Terms of use
[Download the terms as a PDF](https://github.com/navikt/familie-ks-barnehagelister/raw/main/v1.1%20Bruksvilk%C3%A5r%20NAVs%20API%20for%20elektronisk%20innsending%20av%20barnehagelister.pdf)
 

### Submission frequency   
Kindergarten lists should be submitted before or on the 1st of every month. There is a short period for correcting the data. 
The deadline for submitting the list for the current month is the 10th. A municipality may 
 have several submissions within the same month to allow for corrections. 

### API Overview
- An endpoint is available for submitting kindergarten lists, supporting multiple kindergartens per request. Each 
submission must include a unique identifier.
- A separate endpoint is provided for retrieving the submission status. This endpoint will indicate any validation 
errors that require attention.
- Maximum size for data sent in is 5 MB. The application will not accept larger files.

### Authentication  
Access to this API requires authentication via a **Maskinporten** token, utilizing the delegation API for Maskinporten. 
Municipalities must delegate access to their respective suppliers. New suppliers must contact **Nav** to obtain access 
to the API.  
 
The required token scope:  
```
nav:familie/v1/kontantstotte/barnehagelister
```  
Further details are available at: [Maskinporten Delegation API Documentation](https://docs.digdir.no/docs/Maskinporten/maskinporten_func_delegering).  

### Validation errors
The API will return a 400 Bad Request if the JSON is invalid. The response is based on RFC7807 Problem Details for HTTP 
APIs. 
 
If a required key in the json is missing, the response will be a 400 Bad Request with a JSON body containing the error 
in the detail field:  
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
When posting the list the API will return a 200 OK if the JSON is valid, but there might be other issues with the data. 
In those cases the status endpoint will return a JSON body with a list of warnings. 
 
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

### Frequently asked questions
| Question | Answer |
| :--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :---------------------------------: |
| What should be sent in? | Information from the regulation §2 a-e. |
| Should the municipality send in information both for private and public kindergartens? | Yes, the municipality should send in information for all kindergartens in the municipality, including private ones. The municipality is responsible for reporting for all kindergartens in the municipality, including those where the municipality is not the owner. |
| Should we send in hours used or hours allocated? | Hours allocated! It is important that it is not hours used. |
| What if the allocated hours change during a month? | Then we need information about both periods with allocated hours, and you should submit the information as two periods for the same child. NAV will validate that we do not have overlapping periods from the same kindergarten for the same child. |
| What if there are allocated hours in different kindergartens - e.g. during the transition when changing kindergartens? | We handle that in NAV, the most important thing is that there are no overlapping periods in allocated hours from the same kindergarten. |
| How often should the list be sent? | Once a month. |
| When should it be sent? | At the beginning of the month for the current month. We preferably want to have the list by (or on) the 1st of each month, but the absolute latest deadline is by the 10th of the current month. |
| What if there are changes in allocated hours in the current month, should we then send changes? | Yes, submit a new updated list for the month. |
| Should we send information about both parents or only one parent, and should we send addresses and parents from the National Registry (Folkeregisteret)? | We assume that kindergartens have information about who the child lives with, which is what the regulation says NAV should receive information about. You can submit information about multiple parents (persons the child lives with), if the kindergarten has information about this. This is especially useful if the parents have different addresses. You do not need to verify that this is correct according to the National Registry, NAV takes responsibility for doing this. |
| We have some edge cases regarding apartment numbers, e.g. HXXX - here we do not always capture the apartment number. Is that okay? | That is perfectly fine, send the address you (the kindergarten) have as you have it. |
| Should we send information about institutions where the child lives in an institution? | Children in institutions do not receive cash-for-care benefit(kontantstøtte) according to the law. Therefore, we should not have information about this. |
| We have children with synthetic / approximate ID numbers, should we send these? | Yes. |
| How many children are in a kindergarten list? | There is no minimum or maximum number. |
| What should we do if a kindergarten has no children? | If a kindergarten has no children with a kindergarten place in a month, you do not need to submit. You can also submit an empty list. | 
| What does NAV need regarding representation for the kindergartens - business certificate? Do we need authorization per kindergarten or per municipality? | The municipalities delegate representation to you in Maskinporten. When we receive the lists from you, it is thus already verified via Maskinporten that you represent and submit "on behalf of" the municipalities. |
| What statuses do we get back from you when we submit? | See OpenAPI documentation below and "Validation Errors" above. |
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
