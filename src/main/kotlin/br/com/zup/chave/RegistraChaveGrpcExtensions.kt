package br.com.zup.chave

import br.com.zup.RegistrarChaveRequest
import br.com.zup.chave.Enum.TipoDaChave
import br.com.zup.servicosExternos.BankAccount
import br.com.zup.servicosExternos.ChavePixRequestBCB
import br.com.zup.servicosExternos.ErpItauObterClienteResponse
import br.com.zup.servicosExternos.Owner


fun RegistrarChaveRequest.toModel(
    randomUUID: String,
    tipoChave: TipoDaChave,
    erpResponse: ErpItauObterClienteResponse
): Chave {
    var chaveValor = this.valorChave
    val titular = Titular(erpResponse.titular)
    val instituicao = Instituicao(erpResponse.instituicao)

    if (this.valorChave.isBlank() && tipoChave == TipoDaChave.CHAVEALEATORIA) {
        chaveValor = randomUUID
    }
    return Chave(
        tipoChave,
        chaveValor,
        erpResponse.tipo,
        erpResponse.agencia,
        erpResponse.numero,
        titular = titular,
       instituicao = instituicao
    )
}

fun RegistrarChaveRequest.criaChavePixRequestBCB(
    request: RegistrarChaveRequest,
    erpResponse: ErpItauObterClienteResponse
): ChavePixRequestBCB {
    var tipoChave = request.tipoChave

    when {
        tipoChave == "CELULAR" -> tipoChave = "PHONE"
        tipoChave == "CHAVEALEATORIA" -> tipoChave = "RANDOM"
    }

    val chavePixRequestBCB = ChavePixRequestBCB(
        tipoChave,
        request.valorChave,
        BankAccount(
            erpResponse.instituicao.ispb,
            erpResponse.agencia,
            erpResponse.numero, "CACC"
        ),
        Owner("NATURAL_PERSON", erpResponse.titular.nome, erpResponse.titular.cpf)
    )
    return chavePixRequestBCB
}
