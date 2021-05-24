package br.com.zup.chave

import br.com.zup.RegistrarChaveRequest
import br.com.zup.RegistroChaveResponse
import br.com.zup.TipoDeConta
import br.com.zup.chave.Enum.TipoDaChave
import br.com.zup.compartilhado.exception.ChavePixException
import br.com.zup.servicosExternos.ErpItau
import br.com.zup.servicosExternos.ErpItauObterClienteResponse
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.reactivex.Single
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class RegistraChaveRequestValida {


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
                            TipoDaChave.todasChavesString
                        }"
                    )
                    .asRuntimeException()
            )
            return

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


        when {

            //Verificando se chave já está cadastrada no banco de dados
            !request?.valorChave.isNullOrBlank() &&
                    (chaveRepository.existsByTitularIdTitularAndTipoDaChave(
                        request.idCliente,
                        TipoDaChave.valueOf(request.tipoChave)
                    )
                            || chaveRepository.existsByValorDaChave(request.valorChave)
                            )
            -> {
                responseObserver?.onError(Status.ALREADY_EXISTS.withDescription("Está chave já está cadastrada").asRuntimeException())
                return
            }
        }

    }
}
