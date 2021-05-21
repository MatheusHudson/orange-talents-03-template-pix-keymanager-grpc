package br.com.zup.servicosExternos

import br.com.zup.compartilhado.exception.ChavePixException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.slf4j.Logger


data class ChavePixRequestBCB(val keyType: String, val key: String, val bankAccount :BankAccount , val  owner : Owner) {



    fun validaChaveIgual(pixChaveBCB: PixChaveBCB, logger: Logger) {




//            if (validaChaveIgual != null) {

//        } catch (e: HttpClientResponseException) {
//            logger.info("BCB ainda não possui essa chave cadastrada! Prosseguindo com cadastro.")
//        }


    }
}

data class BankAccount(val participant: String, val branch: String, val accountNumber: String, val accountType : String)

data class Owner(val type: String, val name: String, val taxIdNumber: String)