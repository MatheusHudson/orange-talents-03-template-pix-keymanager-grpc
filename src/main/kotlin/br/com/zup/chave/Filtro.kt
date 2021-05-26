package br.com.zup.chave

import br.com.zup.BuscarChavePixResponse
import br.com.zup.compartilhado.exception.ChavePixException
import br.com.zup.servicosExternos.PixChaveBCB
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import java.lang.IllegalArgumentException
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

sealed class Filtro {

    @Introspected
    data class PorPixId(
        @field:NotBlank val pixId: String,
        @field:NotBlank val identificadorCliente: String
    ) : Filtro() {
        override fun buscarChave(chaveRepository: ChaveRepository, pixChaveBCB: PixChaveBCB): BuscarChavePixResponse {

            return chaveRepository.findByIdAndTitularIdTitular(pixId, identificadorCliente).map { chave ->
                criaResponse(chave)
            }.orElseThrow { ChavePixException("Chave não foi encontrada!") }
        }
    }

    @Introspected
    data class ChavePix(@field:NotBlank @field:Size(max = 77) val chavePix: String) : Filtro() {

        override fun buscarChave(chaveRepository: ChaveRepository, pixChaveBCB: PixChaveBCB): BuscarChavePixResponse {
            val optionalChave = chaveRepository.findByValorDaChave(chavePix)

            when {
                optionalChave.isPresent -> return criaResponse(optionalChave.get())

                else -> {
                    val buscarChavePix = pixChaveBCB.buscarChavePix(chavePix)

                    if (buscarChavePix.status == HttpStatus.OK) {
                        val chavePix = buscarChavePix.body()
                        return BuscarChavePixResponse.newBuilder()
                            .setNome(chavePix.owner.name)
                            .setAgencia(chavePix.bankAccount.branch)
                            .setCpf(chavePix.owner.taxIdNumber)
                            .setCreatedAt(chavePix.createdAt)
                            .setNomeBanco(chavePix.bankAccount.participant)
                            .setNumeroConta(chavePix.bankAccount.accountNumber)
                            .setTipoDaChave(chavePix.keyType)
                            .setTipoDaConta(chavePix.bankAccount.accountType)
                            .setValorDaChave(chavePix.key)
                            .build()
                    } else {
                        throw ChavePixException("Chave não foi encontrada!")
                    }
                }
            }

        }
    }

    abstract fun buscarChave(chaveRepository: ChaveRepository, pixChaveBCB: PixChaveBCB): BuscarChavePixResponse

    fun criaResponse(chave: Chave) = BuscarChavePixResponse.newBuilder()
        .setNome(chave.titular.nomeTitular)
        .setAgencia(chave.agencia)
        .setCpf(chave.titular.cpf)
        .setCreatedAt(chave.createdAt.toString())
        .setNomeBanco(chave.instituicao.nomeBanco)
        .setNumeroConta(chave.numeroConta)
        .setTipoDaChave(chave.tipoDaChave.name)
        .setTipoDaConta(chave.tipoConta)
        .setValorDaChave(chave.valorDaChave)
        .build()
}