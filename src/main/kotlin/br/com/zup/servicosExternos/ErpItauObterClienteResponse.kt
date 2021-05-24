package br.com.zup.servicosExternos

import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank


data class ErpItauObterClienteResponse( val titular: Titular, val instituicao: Instituicao, val agencia: String,val numero:String,val tipo:String)

@Embeddable
data class Titular(@field:NotBlank val id: String, @field:NotBlank val  nome: String, @field:NotBlank val cpf: String)

@Embeddable
data class Instituicao(@field:NotBlank val ispb: String,@field:NotBlank val nome: String)