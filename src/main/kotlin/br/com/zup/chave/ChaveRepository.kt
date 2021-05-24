package br.com.zup.chave

import br.com.zup.chave.Enum.TipoDaChave
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChaveRepository : JpaRepository<Chave, String> {


    fun findByValorDaChave(valorDaChave: String): Optional<Chave>
    fun findByIdAndTitularIdTitular(id: String, idTitular: String): Optional<Chave>
    fun existsByTitularIdTitularAndTipoDaChave(idClienteItau: String, tipoDaChave: TipoDaChave): Boolean
    fun existsByTitularIdTitularAndValorDaChaveAndTipoDaChave(
        idTitular: String,
        valorDaChave: String,
        tipoDaChave: TipoDaChave
    ): Boolean

    fun existsByValorDaChave(valorChave: String): Boolean
    fun deleteByValorDaChave(valorDaChave: String)

}
