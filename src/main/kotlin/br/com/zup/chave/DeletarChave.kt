package br.com.zup.chave

import br.com.zup.DeletarChaveRequest
import br.com.zup.DeletarChaveResponse
import br.com.zup.DeletarChaveServiceGrpc
import br.com.zup.chave.Enum.TipoDaChave
import br.com.zup.compartilhado.exception.ChavePixException
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.Valid


@Singleton
@Validated
class DeletarChave(val chaveRepository: ChaveRepository) : DeletarChaveServiceGrpc.DeletarChaveServiceImplBase() {

    override fun deletarChave(request: DeletarChaveRequest?, responseObserver: StreamObserver<DeletarChaveResponse>?) {

        val logger = LoggerFactory.getLogger(this::class.java)


        val requestValido = request.isValid(
            DeletaChaveValidaRequest(
                request!!.pixId,
                request!!.identificadorCliente,
                request!!.tipoChave
            )
        )

        when {

            chaveRepository.existsByIdClienteItauAndIdAndTipoDaChave(
                requestValido.identificadorCliente,
                requestValido.pixId,
                TipoDaChave.valueOf(requestValido.tipoDaChave!!)
            ) -> {
                logger.info("Chave ${request.pixId} deletada com sucesso!")

                chaveRepository.deleteById(requestValido.pixId)
                val response = DeletarChaveResponse.newBuilder()
                    .setMessage("Chave deletada com sucesso!")
                    .build()
                responseObserver?.onNext(response)
                responseObserver?.onCompleted()
            }

        }
        logger.info("Erro ao tentar deletar a chave ${requestValido.pixId} ")

       responseObserver?.onError(Status.NOT_FOUND
           .withDescription("Não foi encontrado nenhuma chave para os dados informados!")
           .asRuntimeException())

        return

    }

     fun DeletarChaveRequest?.isValid(@Valid deletaChaveValidaRequest: DeletaChaveValidaRequest): DeletaChaveValidaRequest {

        return deletaChaveValidaRequest
    }

}


