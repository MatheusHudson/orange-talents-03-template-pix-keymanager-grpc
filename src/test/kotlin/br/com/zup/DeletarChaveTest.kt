package br.com.zup

import br.com.zup.chave.Chave
import br.com.zup.chave.ChaveRepository
import br.com.zup.chave.Enum.TipoDaChave
import br.com.zup.servicosExternos.ErpItau
import io.grpc.ManagedChannel
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import java.util.stream.Stream
import javax.inject.Singleton


@MicronautTest(transactional = false)
class DeletarChaveTest(
    val chaveRepository: ChaveRepository,
    val grpc: DeletarChaveServiceGrpc.DeletarChaveServiceBlockingStub
) {


    @BeforeEach
    fun setup() {
        chaveRepository.deleteAll()
    }

    @DisplayName("deveriaDeletarUmaChavePix")
    @Test
    fun test1() {
        val chave = Chave("c56dfef4-7901-44fb-84e2-a2cefb157890", TipoDaChave.CPF, "02414521663")
        chaveRepository.save(chave)

        val request = DeletarChaveRequest.newBuilder()
            .setPixId(chave.id)
            .setIdentificadorCliente(chave.idClienteItau)
            .setTipoChave("CPF")
            .build()

        val response: DeletarChaveResponse = grpc.deletarChave(request)

        assertEquals("Chave deletada com sucesso!",response.message)
    }

    @DisplayName("naoDeveriaDeletarUmaChavePixSeOIdEstiverErrado")
    @ParameterizedTest
    @MethodSource("criandoArgumentosParaOTeste2")
    fun test2(chave:Chave, request:DeletarChaveRequest) {
        chaveRepository.save(chave)

        val assertThrows = assertThrows<StatusRuntimeException> {
           grpc.deletarChave(request)
        }

        with(assertThrows) {
            assertEquals("NOT_FOUND: Não foi encontrado nenhuma chave para os dados informados!", message)

        }


    }


    companion object {

        @JvmStatic
        fun criandoArgumentosParaOTeste2(): Stream<Arguments> {
            val chave = Chave("c56dfef4-7901-44fb-84e2-a2cefb157890", TipoDaChave.CPF, "02414521663")


            val requestIdInvalido = DeletarChaveRequest.newBuilder()
                .setPixId(chave.id + "1")
                .setIdentificadorCliente(chave.idClienteItau)
                .setTipoChave("CPF")
                .build()

            val requestIdentificadorInvalido = DeletarChaveRequest.newBuilder()
                .setPixId(chave.id)
                .setIdentificadorCliente(chave.idClienteItau + "1")
                .setTipoChave("CPF")
                .build()



            return Stream.of(
                Arguments.of(chave, requestIdInvalido),
                Arguments.of(chave, requestIdentificadorInvalido),
            )


        }

    }


    @MockBean(ErpItau::class)
    fun itauCliente(): ErpItau? {
        return Mockito.mock(ErpItau::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStubDeletar(
            @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
        ): DeletarChaveServiceGrpc.DeletarChaveServiceBlockingStub? {
            return DeletarChaveServiceGrpc.newBlockingStub(channel)
        }
    }
}

