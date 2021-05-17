package br.com.zup.chave

import br.com.zup.chave.Enum.TipoDaChave
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChaveRepository: JpaRepository<Chave, String> {


    fun findByIdClienteitauAndTipoDaChave(valorDaChave: String, tipoDaChave:TipoDaChave) : Optional<Chave>

}