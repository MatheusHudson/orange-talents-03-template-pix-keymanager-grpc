package br.com.zup.chave

import br.com.zup.RegistrarChaveRequest
import br.com.zup.RegistrarChaveServiceGrpc
import br.com.zup.RegistroChaveResponse
import br.com.zup.chave.Enum.TipoDaChave
import br.com.zup.compartilhado.exception.ChavePixException
import br.com.zup.servicosExternos.*
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import io.reactivex.Single
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.net.ConnectException
import javax.annotation.meta.When
import javax.inject.Singleton
import javax.validation.Valid
import kotlin.concurrent.thread
import kotlin.math.log


@Singleton
@Validated
class RegistrarChave(
    val erpItau: ErpItau,
    val chaveRepository: ChaveRepository,
    val chaveRequestValida: RegistraChaveRequestValida,
    val pixChaveBCB: PixChaveBCB
) :
    RegistrarChaveServiceGrpc.RegistrarChaveServiceImplBase() {

    lateinit var response: RegistroChaveResponse
    lateinit var chaveModel: Chave


    override fun registrarChave(
        request: RegistrarChaveRequest?,
        responseObserver: StreamObserver<RegistroChaveResponse>?
    ) {

        val logger = LoggerFactory.getLogger(RegistrarChave::class.java)

        logger.info("Iniciando validação")

        chaveRequestValida.validaRequest(
            request?.idCliente,
            request,
            responseObserver,
            chaveRepository,
            erpItau
        )

        logger.info("Validação finalizada")

        val tipoChave = TipoDaChave.valueOf(request!!.tipoChave)


        tipoChave.valida(
            request.valorChave
        ) { descricao: String ->
            responseObserver?.onError(
                throw  ChavePixException(descricao)
            )
        }

        erpItau.obterCliente(request!!.idCliente, request.tipoConta).subscribe({ erpResponse ->

            val erpResponses = erpResponse.body()

            val chavePixRequestBCB = request.criaChavePixRequestBCB(request, erpResponses)



            pixChaveBCB.registrarChavePixNoBCB(chavePixRequestBCB).subscribe({ pixResponse ->

                val registrarChavePixNoBCB = pixResponse.body()

                chaveModel = validaModel(request.toModel(registrarChavePixNoBCB.key, tipoChave, erpResponses))

                var chave = request.valorChave
                if (chave.isBlank())
                    chave = registrarChavePixNoBCB.key

                response = RegistroChaveResponse.newBuilder()
                    .setChave(chave)
                    .setPixId(chaveModel.id)
                    .setTipoChave(tipoChave.name)
                    .build()

                chaveRepository.save(chaveModel)
                responseObserver?.onNext(response)
                responseObserver?.onCompleted()
            }, {exception ->
                when {
                    exception is HttpClientResponseException -> responseObserver?.onError(
                        Status.INVALID_ARGUMENT.withDescription(
                            "Dados invalidos ou chave já cadastrada!"
                        ).asRuntimeException()
                    )
                    exception is HttpClientException -> responseObserver?.onError(
                        Status.UNAVAILABLE.withDescription("Não foi possivel realizar conexão com o serviço externo")
                            .asRuntimeException()
                    )
                }
            })


        }, { exception ->
            logger.info("Exception",exception)

            when {
                exception is HttpClientResponseException -> responseObserver?.onError(
                    Status.INVALID_ARGUMENT.withDescription(
                        "Dados invalidos!"
                    ).asRuntimeException()
                )
                exception is HttpClientException -> responseObserver?.onError(
                    Status.UNAVAILABLE.withDescription("Não foi possivel realizar conexão com o serviço externo")
                        .asRuntimeException()
                )
            }
        })
    }


    fun validaModel(@Valid chave: Chave): Chave {
        return chave
    }

}




