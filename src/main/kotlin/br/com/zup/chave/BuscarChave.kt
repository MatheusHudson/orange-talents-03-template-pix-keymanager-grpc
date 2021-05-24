package br.com.zup.chave

import br.com.zup.BuscarChavePixGrpc
import br.com.zup.BuscarChavePixRequest
import br.com.zup.BuscarChavePixRequest.FiltroCase.*
import br.com.zup.BuscarChavePixResponse
import br.com.zup.compartilhado.exception.ChavePixException
import br.com.zup.servicosExternos.PixChaveBCB
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
@Validated
class BuscarChave(val chaveRepository: ChaveRepository, val pixChaveBCB: PixChaveBCB) :
    BuscarChavePixGrpc.BuscarChavePixImplBase() {

    override fun buscarUmaChavePix(
        request: BuscarChavePixRequest?,
        responseObserver: StreamObserver<BuscarChavePixResponse>?
    ) {

        val filtro = request.toModel()
        val chaveResponse  = filtro.buscarChave(chaveRepository, pixChaveBCB)

        responseObserver?.onNext(chaveResponse)
        responseObserver?.onCompleted()


    }

     fun BuscarChavePixRequest?.toModel(): Filtro {

        val filtro = when (this?.filtroCase) {

            BUSCARCHAVE -> {

                validaRequest(Filtro.PorPixId(pixId = buscarChave.pixId, buscarChave.identificadorCliente))
            }

            CHAVEPIX ->  {
                validaRequest(Filtro.ChavePix(chavePix))
            }

            else ->  throw ChavePixException("Dados invalidos, preencha a requisição!")

        }


        return filtro

    }

     fun BuscarChavePixRequest?.validaRequest(@Valid porPix: Filtro.PorPixId): Filtro {

        return porPix
    }
    fun BuscarChavePixRequest?.validaRequest(@Valid chavePix: Filtro.ChavePix): Filtro {

        return chavePix
    }


}


