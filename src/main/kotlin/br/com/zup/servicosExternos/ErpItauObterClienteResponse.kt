package br.com.zup.servicosExternos

data class ErpItauObterClienteResponse(val titular: Titular)

data class Titular(val id: String, val nome: String)