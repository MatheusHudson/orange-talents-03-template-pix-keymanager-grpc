package br.com.zup.compartilhado.handler

import br.com.zup.compartilhado.exception.ChavePixException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixExceptionHandler : ExceptionHandler<ChavePixException> {

    override fun handle(e: ChavePixException): ExceptionHandler.StatusWithDetails {

        if(e.message == "Está chave já está cadastrada") {
            return ExceptionHandler.StatusWithDetails(
                Status.ALREADY_EXISTS
                    .withDescription(e.message)
                    .withCause(e)
            )
        }

        return ExceptionHandler.StatusWithDetails(
            Status.INVALID_ARGUMENT
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixException
    }
}