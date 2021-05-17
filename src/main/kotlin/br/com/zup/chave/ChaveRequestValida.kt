package br.com.zup.chave

import br.com.zup.RegistrarChaveRequest
import br.com.zup.RegistroChaveResponse
import br.com.zup.TipoDeConta
import br.com.zup.chave.Enum.TipoDaChave
import br.com.zup.compartilhado.ChavePixException
import br.com.zup.servicosExternos.ErpItau
import br.com.zup.servicosExternos.ErpItauObterClienteResponse
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientException
import io.reactivex.Single
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class ChaveRequestValida {

    fun validaRequest(

        idcliente: String?,
        request: RegistrarChaveRequest?,
        responseObserver: StreamObserver<RegistroChaveResponse>?,
        chaveRepository: ChaveRepository,
        erpItau: ErpItau
    ) {
        val logger = LoggerFactory.getLogger(this::class.java)



        //Valida se o tipo de chave informado está presente no ENUM.
        try {
            val tipoChave = TipoDaChave.valueOf(request!!.tipoChave)
        } catch (e: IllegalArgumentException) {
            responseObserver?.onError(
                Status.INVALID_ARGUMENT.withDescription("Informe um tipo de chave valido!")
                    .augmentDescription(
                        "Formatos aceitos : ${
                            TipoDaChave.values().map { valoresEnum ->
                                valoresEnum.name
                            }
                        }".substringBeforeLast(", UNRECOGNIZED") + "]"
                    )
                    .asRuntimeException()
            )
            return

        }


        //Valida se o id cliente informado está presente  no sistema do Itau
        try {
            logger.info("Try inicio")

            val erpResponse: Single<HttpResponse<ErpItauObterClienteResponse>> =
                erpItau.obterCliente(request!!.idCliente)
            logger.info("Try fim")
        } catch (e: HttpClientException) {
            logger.info("Catch")

            responseObserver?.onError(
                Status.NOT_FOUND.withCause(e).withDescription("Id do cliente não encontrado!")
                    .asRuntimeException()
            )
            return
        }



        when {

            //Verificando se chave já está cadastrada no banco de dados
            request?.valorChave != null &&
                    chaveRepository.findByIdClienteitauAndTipoDaChave(
                        request.idCliente,
                        TipoDaChave.valueOf(request.tipoChave)
                    ).isPresent
            -> {
                throw ChavePixException("Está chave já está cadastrada")
            }
        }


        //Valida se o tipo de conta informado está presente no ENUM.
        try {
            val tipoDeConta = TipoDeConta.valueOf(request!!.tipoConta)
        } catch (e: IllegalArgumentException) {
            responseObserver?.onError(
                Status.INVALID_ARGUMENT.withDescription("Informe um tipo de conta valido!")
                    .augmentDescription(
                        "Formatos aceitos : ${
                            TipoDeConta.values().map { tipoDeConta -> tipoDeConta.name }
                        }".substringBeforeLast(", UNRECOGNIZED") + "]"
                    )
                    .asRuntimeException()
            )

            return
        }
    }
}