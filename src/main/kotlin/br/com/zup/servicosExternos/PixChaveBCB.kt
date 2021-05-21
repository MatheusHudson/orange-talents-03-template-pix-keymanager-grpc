package br.com.zup.servicosExternos

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import io.reactivex.Single

@Client("\${my.server.url.pixBCB:`http://localhost:8082`}")
interface PixChaveBCB {

    @Post("/api/v1/pix/keys", consumes = [MediaType.APPLICATION_XML], produces = [MediaType.APPLICATION_XML])
    fun registrarChavePixNoBCB(@Body chavePixRequestBCB: ChavePixRequestBCB ) : Single<HttpResponse<ChavePixResponseBCBdata>>

    @Get("/api/v1/pix/keys/{key}", consumes = [MediaType.APPLICATION_XML], produces = [MediaType.APPLICATION_XML])
    fun  buscarChavePix(@PathVariable key: String) : Single<HttpResponse<ChavePixResponseBCBdata>>

    @Delete("/api/v1/pix/keys/{key}", consumes = [MediaType.APPLICATION_XML], produces = [MediaType.APPLICATION_XML])
    fun excluirChavePix(@PathVariable key: String, @Body deletePixRequest : DeletePixRequest) :Single<ChaveDeleteResponse>
}


data class ChaveDeleteResponse(val key: String, val participant: String, val deletedAt: String)

data class DeletePixRequest(val key: String, val participant: String )

data class ChavePixResponseBCBdata (val keyType: String, val key: String, val bankAccount :BankAccount , val  owner : Owner, val createdAt: String)

