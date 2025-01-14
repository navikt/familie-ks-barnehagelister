package no.nav.familie.ks.barnehagelister.validering

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Constraint(validatedBy = [FødselsnummerValidator::class])
@Target(AnnotationTarget.FIELD)
annotation class Fødselsnummer(
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<*>> = [],
    val message: String = "Fødselsnummer er ikke ugyldig",
)

class FødselsnummerValidator : ConstraintValidator<Fødselsnummer, String> {
    override fun isValid(
        fødselsnummer: String,
        context: ConstraintValidatorContext?,
    ): Boolean =
        try {
            no.nav.familie.kontrakter.felles
                .Fødselsnummer(fødselsnummer)
            true
        } catch (e: IllegalStateException) {
            false
        }
}
