package br.com.zup

import br.com.zup.chave.Chave
import br.com.zup.chave.ChaveRepository
import br.com.zup.chave.Enum.TipoDaChave
import br.com.zup.servicosExternos.*
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
import java.util.*
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton


@MicronautTest(transactional = false)
class BuscarListaChaveTest(
    val chaveRepository: ChaveRepository,
    val grpc: BuscarListaDeChavePixGrpc.BuscarListaDeChavePixBlockingStub
) {

    @Inject
    lateinit var pixChaveBCB: PixChaveBCB

    val titular = Titular("c56dfef4-7901-44fb-84e2-a2cefb157890", "Rafael M C Ponte", "02467781054")
    val instituicao = Instituicao("60701190", "ITAU BANK")

    @BeforeEach
    fun setup() {
        chaveRepository.deleteAll()
    }

    @DisplayName("deveriaBuscarUmaChavePorPixId")
    @Test
    fun test1() {

        val chave = Chave(
            TipoDaChave.CPF,
            "02414521663",
            "CONTA_CORRENTE",
            "0001",
            "291900",
            br.com.zup.chave.Titular(titular),
            br.com.zup.chave.Instituicao(instituicao)
        )
        chaveRepository.save(chave)

        val chaveRequest = BuscarListaChaveRequest.newBuilder()
            .setCodigoInternoCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .build()

        val response = grpc.buscarListaDeChave(chaveRequest)

        assertEquals("c56dfef4-7901-44fb-84e2-a2cefb157890", response.clienteId)
    }


    @DisplayName("naoDeveriaBuscarUmaChavePorRequestVazia")
    @Test
    fun test2() {

        val chaveRequest = BuscarListaChaveRequest.newBuilder()
            .build()

        val assertThrows = assertThrows<StatusRuntimeException> {
            grpc.buscarListaDeChave(chaveRequest)
        }

        with(assertThrows) {
            assertEquals("INVALID_ARGUMENT: Codigo do cliente não pode ser nulo!", message)
        }

    }

    @MockBean(PixChaveBCB::class)
    fun pixChaveBCB(): PixChaveBCB? {
        return Mockito.mock(PixChaveBCB::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStubBuscarListaChave(
            @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
        ): BuscarListaDeChavePixGrpc.BuscarListaDeChavePixBlockingStub? {
            return BuscarListaDeChavePixGrpc.newBlockingStub(channel)
        }
    }
}

