package br.com.zup.chave

import br.com.zup.RegistrarChaveRequest
import br.com.zup.RegistroChaveResponse
import br.com.zup.TipoDeConta
import br.com.zup.chave.Enum.TipoDaChave
import br.com.zup.compartilhado.exception.ChavePixException
import br.com.zup.servicosExternos.ErpItau
import io.grpc.Status
import io.grpc.stub.StreamObserver
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
            throw ChavePixException(
                "Informe um tipo de chave valido!" +
                        "\nFormatos aceitos : ${
                            TipoDaChave.todasChavesString
                        }"
            )
        }


        //Valida se o tipo de conta informado está presente no ENUM.
        try {
            val tipoDeConta = TipoDeConta.valueOf(request!!.tipoConta)
        } catch (e: IllegalArgumentException) {
            throw ChavePixException("Informe um tipo de conta valido!" + "\nFormatos aceitos : ${
                TipoDeConta.values().map { tipoDeConta -> tipoDeConta.name }
            }".substringBeforeLast(", UNRECOGNIZED") + "]"
            )

        }


        when {

            //Verificando se chave já está cadastrada no banco de dados
            request?.tipoChave != "CHAVEALEATORIA" &&
                    (chaveRepository.existsByTitularIdTitularAndTipoDaChave(
                        request.idCliente,
                        TipoDaChave.valueOf(request.tipoChave)
                    )
                            || chaveRepository.existsByValorDaChave(request.valorChave)
                            )
            -> {
                throw  ChavePixException("Está chave já está cadastrada")
            }
        }

    }
}
