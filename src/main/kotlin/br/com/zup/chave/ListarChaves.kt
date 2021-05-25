package br.com.zup.chave

import br.com.zup.BuscarListaChaveRequest
import br.com.zup.BuscarListaChaveRequestResponse
import br.com.zup.BuscarListaDeChavePixGrpc
import br.com.zup.compartilhado.exception.ChavePixException
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
class ListarChaves(val chaveRepository: ChaveRepository) : BuscarListaDeChavePixGrpc.BuscarListaDeChavePixImplBase() {

    override fun buscarListaDeChave(
        request: BuscarListaChaveRequest?,
        responseObserver: StreamObserver<BuscarListaChaveRequestResponse>?
    ) {

        if (request?.codigoInternoCliente.isNullOrBlank()) {
            throw ChavePixException("Codigo do cliente não pode ser nulo!")
        }

        val chaves = chaveRepository.findAllByTitularIdTitular(request?.codigoInternoCliente).map { chave ->

            BuscarListaChaveRequestResponse.ChavePix.newBuilder()
                .setPixId(chave.id)
                .setIdentificadorCliente(chave.titular.idTitular)
                .setTipoDaChave(chave.tipoDaChave.name)
                .setValorDaChave(chave.valorDaChave)
                .setTipoDeConta(chave.tipoConta)
                .setCreatedAt(chave.createdAt.toString())
                .build()

        }

        val response = BuscarListaChaveRequestResponse.newBuilder()
            .setClienteId(request!!.codigoInternoCliente)
            .addAllListaChave(chaves)
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }
}

