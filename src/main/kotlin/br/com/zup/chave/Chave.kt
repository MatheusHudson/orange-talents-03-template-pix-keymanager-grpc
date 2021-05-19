package br.com.zup.chave

import br.com.zup.chave.Enum.TipoDaChave
import java.util.*
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.validation.constraints.NotBlank

@Entity
class Chave(
    idClienteItau: String,

    @Enumerated(EnumType.STRING)
    val tipoDaChave: TipoDaChave,
    @field:NotBlank val valorDaChave: String
) {

    @Id
    var id : String = UUID.randomUUID().toString()

    @field:NotBlank
    val idClienteItau: String = idClienteItau
}

