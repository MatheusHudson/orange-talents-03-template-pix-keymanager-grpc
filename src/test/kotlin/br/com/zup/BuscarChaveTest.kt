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
class BuscarChaveTest(
    val chaveRepository: ChaveRepository,
    val grpc: BuscarChavePixGrpc.BuscarChavePixBlockingStub
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


        val chaveRequest = BuscarChavePixRequest.newBuilder()
            .setBuscarChave(
                BuscarChavePixRequest.PixBuscar.newBuilder()
                    .setPixId(chave.id)
                    .setIdentificadorCliente(chave.titular.idTitular)
            )
            .build()


        val response = grpc.buscarUmaChavePix(chaveRequest)

        assertEquals("02414521663", response.valorDaChave)
    }


    @DisplayName("deveriaBuscarUmaChavePorValorDaChave")
    @Test
    fun test2() {
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

        val chaveRequest = BuscarChavePixRequest.newBuilder()
            .setChavePix(chave.valorDaChave)
            .build()

        val response = grpc.buscarUmaChavePix(chaveRequest)


        assertEquals("02414521663", response.valorDaChave)
    }

    @DisplayName("deveriaBuscarUmaChavePorValorDaChavePassandoPeloMock")
    @Test
    fun test3() {
        val bankAccount = BankAccount("60701190", "0001", "291900", "CACC")
        val owner = Owner(
            "NATURAL_PERSON", "Rafael M C Ponte", "02467781054"
        )


        val chaveRequest = BuscarChavePixRequest.newBuilder()
            .setChavePix("02268514598")
            .build()


        val chavePixResponseBCBdata = ChavePixResponseBCBdata(
            "CPF",
            "02268514598", bankAccount, owner, LocalDateTime.now().toString()
        )

        Mockito.`when`(pixChaveBCB.buscarChavePix("02268514598"))
            .thenReturn(HttpResponse.ok(chavePixResponseBCBdata))

        val response = grpc.buscarUmaChavePix(chaveRequest)

        assertEquals("02268514598", response.valorDaChave)

    }

    @DisplayName("naoDeveriaBuscarUmaChavePorPixIdComORequestVazio")
    @Test
    fun test4() {

        val chaveRequest = BuscarChavePixRequest.newBuilder()
            .build()


        val assertThrows = assertThrows<StatusRuntimeException> {
            grpc.buscarUmaChavePix(chaveRequest)
        }

        with(assertThrows) {
            assertEquals("INVALID_ARGUMENT: Dados invalidos, preencha a requisição!", message)
        }

    }

    @DisplayName("naoDeveriaBuscarUmaChavePorPixIdQueNaoEstaSalva")
    @Test
    fun test5() {


        val chaveRequest = BuscarChavePixRequest.newBuilder()
            .setBuscarChave(
                BuscarChavePixRequest.PixBuscar.newBuilder()
                    .setPixId(UUID.randomUUID().toString())
                    .setIdentificadorCliente(UUID.randomUUID().toString())
            )
            .build()

        val assertThrows = assertThrows<StatusRuntimeException> {
            grpc.buscarUmaChavePix(chaveRequest)
        }

        with(assertThrows) {
            assertEquals("NOT_FOUND: Chave não foi encontrada!", message)
        }

    }

    @DisplayName("naoDeveriaBuscarUmaChavePorChaveIdQueNaoEstaSalva")
    @Test
    fun test6() {


        val chaveRequest = BuscarChavePixRequest.newBuilder()
            .setChavePix("02268514598")
            .build()


        Mockito.`when`(pixChaveBCB.buscarChavePix("02268514598"))
            .thenReturn(HttpResponse.notFound())

        val assertThrows = assertThrows<StatusRuntimeException> {
            grpc.buscarUmaChavePix(chaveRequest)
        }

        with(assertThrows) {
            assertEquals("NOT_FOUND: Chave não foi encontrada!", message)
        }

    }

    @DisplayName("naoDeveriaBuscarUmaChavePorRequestVazia")
    @Test
    fun test7() {


        val chaveRequest = BuscarChavePixRequest.newBuilder()
            .setChavePix("02268514598")
            .build()


        Mockito.`when`(pixChaveBCB.buscarChavePix("02268514598"))
            .thenReturn(HttpResponse.notFound())

        val assertThrows = assertThrows<StatusRuntimeException> {
            grpc.buscarUmaChavePix(chaveRequest)
        }

        with(assertThrows) {
            assertEquals("NOT_FOUND: Chave não foi encontrada!", message)
        }

    }

    @MockBean(PixChaveBCB::class)
    fun pixChaveBCB(): PixChaveBCB? {
        return Mockito.mock(PixChaveBCB::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStubBuscarChave(
            @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
        ): BuscarChavePixGrpc.BuscarChavePixBlockingStub? {
            return BuscarChavePixGrpc.newBlockingStub(channel)
        }
    }
}

