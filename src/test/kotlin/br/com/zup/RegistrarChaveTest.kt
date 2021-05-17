package br.com.zup

import br.com.zup.chave.ChaveRepository
import br.com.zup.chave.Enum.TipoDaChave
import br.com.zup.servicosExternos.ErpItau
import br.com.zup.servicosExternos.ErpItauObterClienteResponse
import io.grpc.ManagedChannel
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.reactivex.Single
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton


@MicronautTest(transactional = false)
class RegistrarChaveTest(
    val chaveRepository: ChaveRepository,
    val grpc: RegistrarChaveServiceGrpc.RegistrarChaveServiceBlockingStub
) {

    @Inject
    lateinit var itau: ErpItau

    @BeforeEach
    fun setup() {

        Mockito.`when`(itau.obterCliente("c56dfef4-7901-44fb-84e2-a2cefb157890"))
            .thenReturn(Single.create<HttpResponse<ErpItauObterClienteResponse>?> {
                HttpResponse.ok(
                    ErpItauObterClienteResponse("c56dfef4-7901-44fb-84e2-a2cefb157890", "João")
                )
            })
        chaveRepository.deleteAll()
    }

    @DisplayName("deveriaRegistrarUmaChaveParaUmCpfValido")
    @Test
    fun test1() {
        val request = RegistrarChaveRequest.newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave("CPF")
            .setValorChave("39999679015")
            .setTipoConta("CONTACORRENTE")
            .build()

        val response: RegistroChaveResponse = grpc.registrarChave(request)
        assertEquals(request.valorChave, response.chave)
        assertNotNull(response.pixId)

    }

    @DisplayName("deveriaRegistrarUmaChaveParaUmCelularValido")
    @Test
    fun test2() {

        val request = RegistrarChaveRequest.newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave("CELULAR")
            .setValorChave("(99) 98888-8888")
            .setTipoConta("CONTACORRENTE")
            .build()

        val response: RegistroChaveResponse = grpc.registrarChave(request)

        assertEquals(request.valorChave, response.chave)
        assertNotNull(response.pixId)

    }

    @DisplayName("deveriaRegistrarUmaChaveParaUmEmailValido")
    @Test
    fun test3() {

        val request = RegistrarChaveRequest.newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave("EMAIL")
            .setValorChave("test@test.com")
            .setTipoConta("CONTACORRENTE")
            .build()

        val response: RegistroChaveResponse = grpc.registrarChave(request)

        assertEquals(request.valorChave, response.chave)
        assertNotNull(response.pixId)

    }

    @DisplayName("deveriaRegistrarUmaChaveAleatoria")
    @Test
    fun test4() {

        val request = RegistrarChaveRequest.newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave("CHAVEALEATORIA")
            .setValorChave("")
            .setTipoConta("CONTACORRENTE")
            .build()

        val response: RegistroChaveResponse = grpc.registrarChave(request)

        assertNotNull(response.pixId)
    }

    @ParameterizedTest
    @DisplayName("deveRetornarErroParaAsValidacoes")
    @MethodSource("criandoArgumentosParaOTeste5")
    fun test5(mensagemErro: String, request: RegistrarChaveRequest) {

        val thrown = assertThrows<StatusRuntimeException> {
            grpc.registrarChave(request)
        }

        with(thrown) {
            assertEquals(mensagemErro, message)

        }


    }

    companion object {

        @JvmStatic
        fun criandoArgumentosParaOTeste5(): Stream<Arguments> {
            //Chave Aleatoria
            val requestChaveAleatoria = RegistrarChaveRequest.newBuilder()
                .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave("CHAVEALEATORIA")
                .setValorChave("teste")
                .setTipoConta("CONTACORRENTE")
                .build()
            val msgErroChaveAleatoria = "INVALID_ARGUMENT: Para uma chave aleatoria não insira um valor de chave!"

            //Chave CPF
            val requestCpf = RegistrarChaveRequest.newBuilder()
                .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave("CPF")
                .setValorChave("021542141")
                .setTipoConta("CONTACORRENTE")
                .build()
            val msgErroCpf =  "INVALID_ARGUMENT: O cpf informado não corresponde com o formato: 12345678901 !"

            //Chave Email
            val requestEmail = RegistrarChaveRequest.newBuilder()
                .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave("EMAIL")
                .setValorChave("teste.com")
                .setTipoConta("CONTACORRENTE")
                .build()
            val msgErroEmail = "INVALID_ARGUMENT: O email informado não corresponde com o formato: example@..."

            //Chave Celular
            val requestCelular = RegistrarChaveRequest.newBuilder()
                .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave("CELULAR")
                .setValorChave("31985541145")
                .setTipoConta("CONTACORRENTE")
                .build()
            val msgErroCelular = "INVALID_ARGUMENT: O celular informado não corresponde com o formato: (99) 98888-8888 "


            //Tipo de chave informado está presente no ENUM.
            val requestEnum = RegistrarChaveRequest.newBuilder()
                .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave("CELULA")
                .setValorChave("(31) 98888-8888")
                .setTipoConta("CONTACORRENTE")
                .build()

            val msgErroEnum = "INVALID_ARGUMENT: Informe um tipo de chave valido!" + "\nFormatos aceitos : ${
                TipoDaChave.values().map { valoresEnum ->
                    valoresEnum.name
                }
            }".substringBeforeLast(", UNRECOGNIZED") + "]"

            //Tipo de conta informado está presente no ENUM.
            val requestEnunConta = RegistrarChaveRequest.newBuilder()
                .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave("CELULAR")
                .setValorChave("(31) 98888-8888")
                .setTipoConta("")
                .build()

            val msgErroEnumConta = "INVALID_ARGUMENT: Informe um tipo de conta valido!" + "\nFormatos aceitos : ${
                TipoDeConta.values().map { valoresEnum ->
                    valoresEnum.name
                }
            }".substringBeforeLast(", UNRECOGNIZED") + "]"


            return Stream.of(
                Arguments.of(msgErroCpf, requestCpf),
                Arguments.of(msgErroEmail, requestEmail),
                Arguments.of(msgErroCelular, requestCelular),
                Arguments.of(msgErroChaveAleatoria, requestChaveAleatoria),
                Arguments.of(msgErroEnum, requestEnum),
                Arguments.of(msgErroEnumConta, requestEnunConta)
            )


        }

    }

    @DisplayName("deveRetornarErroAoTentaRegistrarUmaChaveJaRegistrada")
    @Test
    fun test6() {


        val request = RegistrarChaveRequest.newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave("CPF")
            .setValorChave("02267244677")
            .setTipoConta("CONTACORRENTE")
            .build()

        val thrown = assertThrows<StatusRuntimeException> {
            grpc.registrarChave(request)
            grpc.registrarChave(request)
        }

        with(thrown) {
            assertEquals("ALREADY_EXISTS: Está chave já está cadastrada", message)

        }
    }

    @DisplayName("deveRegistrarAsChaveParaOMesmoIdClienteSeOTipoDeChaveForDiferente")
    @Test
    fun test7() {

        val requestEmail = RegistrarChaveRequest.newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave("EMAIL")
            .setValorChave("test@test.com")
            .setTipoConta("CONTACORRENTE")
            .build()

        val requestCPF = RegistrarChaveRequest.newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave("CPF")
            .setValorChave("02545621511")
            .setTipoConta("CONTACORRENTE")
            .build()

        val responseCpf: RegistroChaveResponse = grpc.registrarChave(requestCPF)

        val responseEmail: RegistroChaveResponse = grpc.registrarChave(requestEmail)

        assertEquals(requestEmail.valorChave, responseEmail.chave)
        assertEquals(requestCPF.valorChave, responseCpf.chave)
        assertNotNull(responseEmail.pixId)
        assertNotNull(responseCpf.pixId)

    }

    @MockBean(ErpItau::class)
    fun itauCliente(): ErpItau? {
        return Mockito.mock(ErpItau::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(
            @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
        ): RegistrarChaveServiceGrpc.RegistrarChaveServiceBlockingStub? {
            return RegistrarChaveServiceGrpc.newBlockingStub(channel)
        }
    }
}

