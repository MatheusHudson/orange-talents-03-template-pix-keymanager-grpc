package br.com.zup.servicosExternos

data class ErpItauObterClienteResponse(val titular: Titular, val instituicao: Instituicao, val agencia: String, val numero:String)

data class Titular(val id: String, val nome: String, val cpf: String)
data class Instituicao(val ispb: String)