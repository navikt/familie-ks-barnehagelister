package no.nav.familie.ks.barnehagelister.validering

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Constraint(validatedBy = [OrganisasjonsnummerValidator::class])
@Target(AnnotationTarget.FIELD)
annotation class Organisasjonsnummer(
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<*>> = [],
    val message: String = "Not a valid organization number",
)

class OrganisasjonsnummerValidator : ConstraintValidator<Organisasjonsnummer, String> {
    override fun isValid(
        organisasjonsnummer: String,
        context: ConstraintValidatorContext?,
    ): Boolean {
        organisasjonsnummer.trim().apply {
            val valid =
                harOrganisasjonsnummerRiktigLengde(this) &&
                    erOrganisasjonsnummerKunTall(this) &&
                    erGyldigOrganisasjonsnummer(this)

            if (!valid) {
                context?.disableDefaultConstraintViolation()
                context
                    ?.buildConstraintViolationWithTemplate(
                        "Not a valid organization number $this",
                    )?.addConstraintViolation()
            }

            return valid
        }
    }

    private fun erGyldigOrganisasjonsnummer(organisasjonsnummer: String) =
        getKontrollSifferOrganisasjonsnummer(organisasjonsnummer) ==
            organisasjonsnummer
                .trim()
                .last()
                .toString()
                .toInt()

    private fun erOrganisasjonsnummerKunTall(organisasjonsnummer: String) = organisasjonsnummer.all { it.isDigit() }

    private fun harOrganisasjonsnummerRiktigLengde(organisasjonsnummer: String) = (organisasjonsnummer.trim().length == 9)

    private fun getKontrollSifferOrganisasjonsnummer(number: String): Int {
        val lastIndex = number.length - 1
        var sum = 0

        for (i in 0 until lastIndex) {
            sum += Character.getNumericValue(number[i]) * getVektTallOrganisasjonsnummer(i)
        }

        val rest = sum % 11

        return getKontrollSifferOrganisasjonsnummerFraRest(rest)
    }

    private fun getVektTallOrganisasjonsnummer(i: Int): Int {
        val vekttall = intArrayOf(3, 2, 7, 6, 5, 4, 3, 2)
        return vekttall[i]
    }

    private fun getKontrollSifferOrganisasjonsnummerFraRest(rest: Int): Int {
        if (rest == 0) {
            return 0
        }
        return 11 - rest
    }
}
