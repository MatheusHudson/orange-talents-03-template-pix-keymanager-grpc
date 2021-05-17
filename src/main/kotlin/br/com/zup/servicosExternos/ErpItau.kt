package br.com.zup.servicosExternos

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client
import io.reactivex.Single


@Client("\${my.server.url:`http://localhost:9091`}")
interface ErpItau {

        @Get("/api/v1/clientes/{clienteId}")
        fun obterCliente(@PathVariable clienteId: String) : Single<HttpResponse<ErpItauObterClienteResponse>>
}

