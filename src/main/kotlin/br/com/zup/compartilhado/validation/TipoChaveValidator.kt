package br.com.zup.compartilhado.validation

import br.com.zup.chave.Enum.TipoDaChave
import br.com.zup.compartilhado.exception.ChavePixException
import io.micronaut.context.MessageSource
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import java.lang.IllegalArgumentException
import javax.inject.Singleton

import javax.validation.Constraint
import kotlin.annotation.AnnotationRetention.RUNTIME

@Retention(RUNTIME)
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [TipoChaveValidator::class])
annotation class TipoChave(
    val message: String = "Valor ({validatedValue}) informado é invalido!"
)

@Singleton
class TipoChaveValidator : ConstraintValidator<TipoChave, CharSequence> {


    val message: MessageSource = MessageSource.EMPTY



    override fun isValid(

        value: CharSequence?,
        annotationMetadata: AnnotationValue<TipoChave>,
        context: ConstraintValidatorContext): Boolean
    {
        try{
            TipoDaChave.valueOf(value.toString())
        } catch (e: IllegalArgumentException) {
            throw ChavePixException("Valor informado ´é invalido!\n Valores aceitos: " + TipoDaChave.todasChavesString)
        }

        return true
    }
}