package br.com.zup.compartilhado.handler


import io.grpc.Status
import javax.validation.ConstraintViolationException

/**
 * By design, this class must NOT be managed by Micronaut
 */


class DefaultExceptionHandler : ExceptionHandler<Exception> {



    override fun handle(e: Exception): ExceptionHandler.StatusWithDetails {



        val status = when (e) {
            is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message)
            is ConstraintViolationException -> Status.INVALID_ARGUMENT.withDescription(e.message!!.substringAfter(":").substringBefore(","))
            else -> Status.UNKNOWN
        }
        return ExceptionHandler.StatusWithDetails(status.withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return true
    }

}