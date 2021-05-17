package br.com.zup.chave

import br.com.zup.RegistrarChaveRequest
import br.com.zup.chave.Enum.TipoDaChave


fun RegistrarChaveRequest.toModel(randomUUID: String, tipoChave: TipoDaChave): Chave {
        var chaveValor = this.valorChave

        if (this.valorChave.isBlank() && tipoChave == TipoDaChave.CHAVEALEATORIA) {
            chaveValor = randomUUID
        }
        return Chave(this.idCliente, tipoChave, chaveValor)
    }
