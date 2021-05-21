package br.com.zup.chave

import br.com.zup.compartilhado.validation.TipoChave
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
class DeletaChaveValidaRequest(
    @field:NotBlank val valorDaChave: String,
    @field:NotBlank val identificadorCliente: String,
    @field: NotBlank @field:TipoChave val tipoDaChave: String,
    @field: NotBlank val participant: String
)