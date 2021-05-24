package br.com.zup.chave

import br.com.zup.chave.Enum.TipoDaChave
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity
class Chave(
    @Enumerated(EnumType.STRING)
    val tipoDaChave: TipoDaChave,
    @field:NotBlank val valorDaChave: String,
    @field:NotBlank val tipoConta: String,
    @field:NotBlank val agencia: String,
    @field:NotBlank val numeroConta: String,

    @Embedded
    val titular:Titular,

    @Embedded
    val instituicao: Instituicao,

) {

    @Id
    var id : String = UUID.randomUUID().toString()

    @Column(updatable = false)
    val createdAt : LocalDateTime = LocalDateTime.now()

}


@Embeddable
class Titular(titular: br.com.zup.servicosExternos.Titular) {
    @field:NotBlank val idTitular: String = titular.id
    @field:NotBlank val  nomeTitular: String = titular.nome
    @field:NotBlank val cpf: String = titular.cpf
}

@Embeddable
class Instituicao(instituicao: br.com.zup.servicosExternos.Instituicao) {
    @field:NotBlank val ispb: String = instituicao.ispb
    @field:NotBlank val nomeBanco: String = instituicao.nome
}