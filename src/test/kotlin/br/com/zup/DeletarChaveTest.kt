package br.com.zup

import br.com.zup.chave.Chave
import br.com.zup.chave.ChaveRepository
import br.com.zup.chave.Enum.TipoDaChave
import br.com.zup.servicosExternos.ChaveDeleteResponse
import br.com.zup.servicosExternos.DeletePixRequest
import br.com.zup.servicosExternos.ErpItau
import br.com.zup.servicosExternos.PixChaveBCB
import io.grpc.ManagedChannel
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.reactivex.Single
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton


@MicronautTest(transactional = false)
class DeletarChaveTest(
    val chaveRepository: ChaveRepository,
    val grpc: DeletarChaveServiceGrpc.DeletarChaveServiceBlockingStub
) {


    @Inject
    lateinit var pixChaveBCB: PixChaveBCB

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
            .setValorDaChave("02414521663")
            .setIdentificadorCliente(chave.idClienteItau)
            .setTipoChave("CPF")
            .setParticipant("60701190")
            .build()

        val deletePixRequest = DeletePixRequest(request.valorDaChave, request.participant)

        Mockito.`when`(pixChaveBCB.excluirChavePix(request.valorDaChave, deletePixRequest))
            .thenReturn(Single.just(ChaveDeleteResponse(request.valorDaChave, request.participant, LocalDateTime.now().toString())))

        val response: DeletarChaveResponse = grpc.deletarChave(request)

        assertEquals("02414521663", response.chave)
        assertEquals("60701190", response.participant)
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

    @DisplayName("deveriaRetornarErroAoReceberUmHttpResponseException")
    @Test
    fun test3() {
        val chave = Chave("c56dfef4-7901-44fb-84e2-a2cefb157890", TipoDaChave.CPF, "02414521663")
        chaveRepository.save(chave)

        val request = DeletarChaveRequest.newBuilder()
            .setValorDaChave("02414521663")
            .setIdentificadorCliente(chave.idClienteItau)
            .setTipoChave("CPF")
            .setParticipant("60701191")
            .build()

        val deletePixRequest = DeletePixRequest(request.valorDaChave, request.participant)

        Mockito.`when`(pixChaveBCB.excluirChavePix(request.valorDaChave, deletePixRequest))
            .thenReturn(Single.error(HttpClientResponseException("Erro", HttpResponse.notFound("Erro"))))


        val assertThrows = assertThrows<StatusRuntimeException> {
           grpc.deletarChave(request)
        }

        with(assertThrows) {

            assertEquals("NOT_FOUND: Dados de requisição invalido!", assertThrows.message)

        }
    }

    @DisplayName("deveriaRetornarErroAoReceberUmHttpClientException")
    @Test
    fun test4() {
        val chave = Chave("c56dfef4-7901-44fb-84e2-a2cefb157890", TipoDaChave.CPF, "02414521663")
        chaveRepository.save(chave)

        val request = DeletarChaveRequest.newBuilder()
            .setValorDaChave("02414521663")
            .setIdentificadorCliente(chave.idClienteItau)
            .setTipoChave("CPF")
            .setParticipant("60701191")
            .build()

        val deletePixRequest = DeletePixRequest(request.valorDaChave, request.participant)

        Mockito.`when`(pixChaveBCB.excluirChavePix(request.valorDaChave, deletePixRequest))
            .thenReturn(Single.error(HttpClientException("Erro de conexão")))


        val assertThrows = assertThrows<StatusRuntimeException> {
            grpc.deletarChave(request)
        }

        with(assertThrows) {

            assertEquals("UNAVAILABLE: Não foi possivel realizar uma conexão com o serviço externo", assertThrows.message)

        }
    }

    companion object {

        @JvmStatic
        fun criandoArgumentosParaOTeste2(): Stream<Arguments> {
            val chave = Chave("c56dfef4-7901-44fb-84e2-a2cefb157890", TipoDaChave.CPF, "02414521663")


            val requestIdInvalido = DeletarChaveRequest.newBuilder()
                .setValorDaChave(chave.valorDaChave + "1")
                .setIdentificadorCliente(chave.idClienteItau)
                .setTipoChave("CPF")
                .setParticipant("60701190")
                .build()

            val requestIdentificadorInvalido = DeletarChaveRequest.newBuilder()
                .setValorDaChave(chave.valorDaChave)
                .setIdentificadorCliente(chave.idClienteItau + "1")
                .setTipoChave("CPF")
                .setParticipant("60701190")
                .build()

            return Stream.of(
                Arguments.of(chave, requestIdInvalido),
                Arguments.of(chave, requestIdentificadorInvalido),
            )


        }

    }


    @MockBean(PixChaveBCB::class)
    fun pixChaveBCB(): PixChaveBCB? {
        return Mockito.mock(PixChaveBCB::class.java)
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

