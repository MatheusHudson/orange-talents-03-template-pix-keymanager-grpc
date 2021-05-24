package br.com.zup.chave

import br.com.zup.DeletarChaveRequest
import br.com.zup.DeletarChaveResponse
import br.com.zup.DeletarChaveServiceGrpc
import br.com.zup.chave.Enum.TipoDaChave
import br.com.zup.servicosExternos.DeletePixRequest
import br.com.zup.servicosExternos.PixChaveBCB
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.Valid


@Singleton
@Validated
class DeletarChave(val chaveRepository: ChaveRepository, val pixChaveBCB: PixChaveBCB) :
    DeletarChaveServiceGrpc.DeletarChaveServiceImplBase() {

    override fun deletarChave(request: DeletarChaveRequest?, responseObserver: StreamObserver<DeletarChaveResponse>?) {

        val logger = LoggerFactory.getLogger(this::class.java)


        val requestValido = request.isValid(
            DeletaChaveValidaRequest(
                request!!.valorDaChave,
                request.identificadorCliente,
                request.tipoChave,
                request.participant
            )
        )

        when {

            chaveRepository.existsByTitularIdTitularAndValorDaChaveAndTipoDaChave(
                requestValido.identificadorCliente,
                requestValido.valorDaChave,
                TipoDaChave.valueOf(requestValido.tipoDaChave!!)
            ) -> {
                pixChaveBCB.excluirChavePix(
                    requestValido.valorDaChave,
                    DeletePixRequest(requestValido.valorDaChave, requestValido.participant)
                ).subscribe({ chaveResponse ->

                    chaveRepository.deleteByValorDaChave(requestValido.valorDaChave)
                    logger.info("Chave ${requestValido.valorDaChave} deletada com sucesso!")

                    val response = DeletarChaveResponse.newBuilder()
                        .setChave(chaveResponse.key)
                        .setParticipant(chaveResponse.participant)
                        .build()

                    responseObserver?.onNext(response)
                    responseObserver?.onCompleted()

                }, { exception ->
                    logger.info("Erro ao tentar deletar a chave ${requestValido.valorDaChave} ")

                    when {

                        exception is HttpClientResponseException -> responseObserver?.onError(
                            Status.NOT_FOUND
                                .withDescription("Dados de requisição invalido!")
                                .asRuntimeException()
                        )

                        exception is HttpClientException -> responseObserver?.onError(
                            Status.UNAVAILABLE.withDescription("Não foi possivel realizar uma conexão com o serviço externo")
                                .asRuntimeException()
                        )
                    }
                })
                return
            }
        }

        responseObserver?.onError(
        Status.NOT_FOUND
            .withDescription("Não foi encontrado nenhuma chave para os dados informados!")
            .asRuntimeException()
        )

    }

    fun DeletarChaveRequest?.isValid(@Valid deletaChaveValidaRequest: DeletaChaveValidaRequest): DeletaChaveValidaRequest {

        return deletaChaveValidaRequest
    }

}


