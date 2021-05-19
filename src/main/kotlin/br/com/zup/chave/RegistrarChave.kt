package br.com.zup.chave

import br.com.zup.RegistrarChaveRequest
import br.com.zup.RegistrarChaveServiceGrpc
import br.com.zup.RegistroChaveResponse
import br.com.zup.chave.Enum.TipoDaChave
import br.com.zup.compartilhado.exception.ChavePixException
import br.com.zup.servicosExternos.ErpItau
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton
import javax.validation.Valid


@Singleton
@Validated
class RegistrarChave(val erpItau: ErpItau, val chaveRepository: ChaveRepository, val chaveRequestValida: RegistraChaveRequestValida) :
    RegistrarChaveServiceGrpc.RegistrarChaveServiceImplBase() {


    override fun registrarChave(
        request: RegistrarChaveRequest?,
        responseObserver: StreamObserver<RegistroChaveResponse>?
    ) {

        val logger = LoggerFactory.getLogger(RegistrarChave::class.java)
        logger.info("Iniciando validação")
        chaveRequestValida.validaRequest( request?.idCliente, request,responseObserver, chaveRepository, erpItau)
        logger.info("Validação finalizada")

        val tipoChave = TipoDaChave.valueOf(request!!.tipoChave)
        val randomUUID = UUID.randomUUID().toString()

        val chaveModel = request.toModel(randomUUID, tipoChave)

        fun salva(@Valid chave: Chave ) {
            chaveRepository.save(chave)
        }

        tipoChave.processaRequest(
            request.valorChave,
            {
                var chave = request.valorChave
                if (chave.isBlank())
                    chave = randomUUID
                salva(chave = chaveModel)

                val response = RegistroChaveResponse.newBuilder()
                    .setChave(chave)
                    .setPixId(chaveModel.id)
                    .setTipoChave(tipoChave.name)
                    .build()
                responseObserver?.onNext(response)
                responseObserver?.onCompleted()
            },
            { descricao: String ->
                responseObserver?.onError(
                    throw  ChavePixException(descricao)
                )
            })

    }

}


