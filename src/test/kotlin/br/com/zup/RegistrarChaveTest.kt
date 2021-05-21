package br.com.zup

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
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
class RegistrarChaveTest(
    val chaveRepository: ChaveRepository,
    val grpc: RegistrarChaveServiceGrpc.RegistrarChaveServiceBlockingStub
) {

    @Inject
    lateinit var itau: ErpItau

    @Inject
    lateinit var pixChaveBCB: PixChaveBCB

    lateinit var bankAccount:BankAccount
    lateinit var  owner: Owner

    @BeforeEach
    fun setup() {
        bankAccount = BankAccount("60701190", "0001", "291900", "CACC")
        owner = Owner(
            "NATURAL_PERSON", "Rafael M C Ponte", "02467781054"
        )


        Mockito.`when`(itau.obterCliente("c56dfef4-7901-44fb-84e2-a2cefb157890", "CONTA_CORRENTE"))
            .thenReturn(
                Single.just<HttpResponse<ErpItauObterClienteResponse>?>(
                    HttpResponse.ok(
                        ErpItauObterClienteResponse(
                            Titular("c56dfef4-7901-44fb-84e2-a2cefb157890", "Rafael M C Ponte", "02467781054"),
                            Instituicao("60701190"), "0001", "291900"
                        )
                    )
                )
            )
        chaveRepository.deleteAll()
    }

    @DisplayName("deveriaRegistrarUmaChaveParaUmCpfValido")
    @Test
    fun test1() {
        val request = RegistrarChaveRequest.newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave("CPF")
            .setValorChave("39999679015")
            .setTipoConta("CONTA_CORRENTE")
            .build()


        val chavePixRequestBCB = ChavePixRequestBCB(request.tipoChave, request.valorChave, bankAccount, owner)

        val chavePixResponseBCBdata = ChavePixResponseBCBdata(
            request.tipoChave,
            request.valorChave, bankAccount, owner, LocalDateTime.now().toString()
        )


        Mockito.`when`(pixChaveBCB.registrarChavePixNoBCB(chavePixRequestBCB))
            .thenReturn(Single.just(HttpResponse.ok(chavePixResponseBCBdata)))


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
            .setTipoConta("CONTA_CORRENTE")
            .build()


        val chavePixRequestBCB = ChavePixRequestBCB("PHONE", request.valorChave, bankAccount, owner)

        val chavePixResponseBCBdata = ChavePixResponseBCBdata(
            request.tipoChave,
            request.valorChave, bankAccount, owner, LocalDateTime.now().toString()
        )


        Mockito.`when`(pixChaveBCB.registrarChavePixNoBCB(chavePixRequestBCB))
            .thenReturn(Single.just(HttpResponse.ok(chavePixResponseBCBdata)))


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
            .setTipoConta("CONTA_CORRENTE")
            .build()



        val chavePixRequestBCB = ChavePixRequestBCB(request.tipoChave, request.valorChave, bankAccount, owner)

        val chavePixResponseBCBdata = ChavePixResponseBCBdata(
            request.tipoChave,
            request.valorChave, bankAccount, owner, LocalDateTime.now().toString()
        )

        Mockito.`when`(pixChaveBCB.registrarChavePixNoBCB(chavePixRequestBCB))
            .thenReturn(Single.just(HttpResponse.ok(chavePixResponseBCBdata)))

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
            .setTipoConta("CONTA_CORRENTE")
            .build()


        val chavePixRequestBCB = ChavePixRequestBCB("RANDOM", request.valorChave, bankAccount, owner)

        val chavePixResponseBCBdata = ChavePixResponseBCBdata(
            request.tipoChave,
            UUID.randomUUID().toString(), bankAccount, owner, LocalDateTime.now().toString()
        )

        Mockito.`when`(pixChaveBCB.registrarChavePixNoBCB(chavePixRequestBCB))
            .thenReturn(Single.just(HttpResponse.ok(chavePixResponseBCBdata)))


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
                .setTipoConta("CONTA_CORRENTE")
                .build()
            val msgErroChaveAleatoria = "INVALID_ARGUMENT: Para uma chave aleatoria não insira um valor de chave!"

            //Chave CPF
            val requestCpf = RegistrarChaveRequest.newBuilder()
                .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave("CPF")
                .setValorChave("021542141")
                .setTipoConta("CONTA_CORRENTE")
                .build()
            val msgErroCpf = "INVALID_ARGUMENT: O cpf informado não corresponde com o formato: 12345678901 !"

            //Chave Email
            val requestEmail = RegistrarChaveRequest.newBuilder()
                .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave("EMAIL")
                .setValorChave("teste.com")
                .setTipoConta("CONTA_CORRENTE")
                .build()
            val msgErroEmail = "INVALID_ARGUMENT: O email informado não corresponde com o formato: example@..."

            //Chave Celular
            val requestCelular = RegistrarChaveRequest.newBuilder()
                .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave("CELULAR")
                .setValorChave("31985541145")
                .setTipoConta("CONTA_CORRENTE")
                .build()
            val msgErroCelular = "INVALID_ARGUMENT: O celular informado não corresponde com o formato: (99) 98888-8888 "


            //Tipo de chave informado está presente no ENUM.
            val requestEnum = RegistrarChaveRequest.newBuilder()
                .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave("CELULA")
                .setValorChave("(31) 98888-8888")
                .setTipoConta("CONTA_CORRENTE")
                .build()

            val msgErroEnum = "INVALID_ARGUMENT: Informe um tipo de chave valido!" + "\nFormatos aceitos : ${
                TipoDaChave.values().map { valoresEnum ->
                    valoresEnum.name
                }
            }".substringBeforeLast(", UNRECOGNIZED")

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
            .setTipoConta("CONTA_CORRENTE")
            .build()


        val chavePixRequestBCB = ChavePixRequestBCB(request.tipoChave, request.valorChave, bankAccount, owner)

        val chavePixResponseBCBdata = ChavePixResponseBCBdata(
            request.tipoChave,
            request.valorChave, bankAccount, owner, LocalDateTime.now().toString()
        )

        Mockito.`when`(pixChaveBCB.registrarChavePixNoBCB(chavePixRequestBCB))
            .thenReturn(Single.just(HttpResponse.ok(chavePixResponseBCBdata)))


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
            .setTipoConta("CONTA_CORRENTE")
            .build()

        val requestCPF = RegistrarChaveRequest.newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave("CPF")
            .setValorChave("02545621511")
            .setTipoConta("CONTA_CORRENTE")
            .build()

        val chavePixRequestBCBCPF = ChavePixRequestBCB(requestCPF.tipoChave, requestCPF.valorChave, bankAccount, owner)

        val chavePixResponseBCBdataCPF = ChavePixResponseBCBdata(
            requestCPF.tipoChave,
            requestCPF.valorChave, bankAccount, owner, LocalDateTime.now().toString()
        )

        Mockito.`when`(pixChaveBCB.registrarChavePixNoBCB(chavePixRequestBCBCPF))
            .thenReturn(Single.just(HttpResponse.ok(chavePixResponseBCBdataCPF)))

        val chavePixRequestBCBEmail = ChavePixRequestBCB(requestEmail.tipoChave, requestEmail.valorChave, bankAccount, owner)

        val chavePixResponseBCBdataEmail = ChavePixResponseBCBdata(
            requestEmail.tipoChave,
            requestEmail.valorChave, bankAccount, owner, LocalDateTime.now().toString()
        )

        Mockito.`when`(pixChaveBCB.registrarChavePixNoBCB(chavePixRequestBCBEmail))
            .thenReturn(Single.just(HttpResponse.ok(chavePixResponseBCBdataEmail)))




        val responseCpf: RegistroChaveResponse = grpc.registrarChave(requestCPF)

        val responseEmail: RegistroChaveResponse = grpc.registrarChave(requestEmail)

        assertEquals(requestEmail.valorChave, responseEmail.chave)
        assertEquals(requestCPF.valorChave, responseCpf.chave)
        assertNotNull(responseEmail.pixId)
        assertNotNull(responseCpf.pixId)

    }


    @DisplayName("deveRetornarErroPorFaltaDeConexaoAoServicoExternoBCB")
    @Test
    fun test8() {


        val request = RegistrarChaveRequest.newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave("CPF")
            .setValorChave("02267244677")
            .setTipoConta("CONTA_CORRENTE")
            .build()


        val chavePixRequestBCB = ChavePixRequestBCB(request.tipoChave, request.valorChave, bankAccount, owner)

        val chavePixResponseBCBdata = ChavePixResponseBCBdata(
            request.tipoChave,
            request.valorChave, bankAccount, owner, LocalDateTime.now().toString()
        )

        Mockito.`when`(pixChaveBCB.registrarChavePixNoBCB(chavePixRequestBCB))
            .thenReturn(Single.error(HttpClientResponseException("Erro", HttpResponse.badRequest("") )))


        val thrown = assertThrows<StatusRuntimeException> {
            grpc.registrarChave(request)
        }

        with(thrown) {
            assertEquals("INVALID_ARGUMENT: Dados invalidos ou chave já cadastrada!", message)

        }
    }

    @DisplayName("deveRetornarErroPorFaltaDeConexaoAoServicoExternoERP")
    @Test
    fun test9() {


        val request = RegistrarChaveRequest.newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave("CPF")
            .setValorChave("02267244677")
            .setTipoConta("CONTA_CORRENTE")
            .build()


        Mockito.`when`(itau.obterCliente("c56dfef4-7901-44fb-84e2-a2cefb157890", "CONTA_CORRENTE"))
            .thenReturn(
                Single.error(HttpClientException("Erro de conexao"))
            )


        val thrown = assertThrows<StatusRuntimeException> {
            grpc.registrarChave(request)
        }

        with(thrown) {
            assertEquals("UNAVAILABLE: Não foi possivel realizar conexão com o serviço externo", message)

        }
    }

    @MockBean(ErpItau::class)
    fun itauCliente(): ErpItau? {
        return Mockito.mock(ErpItau::class.java)
    }

    @MockBean(PixChaveBCB::class)
    fun pixChaveBCB(): PixChaveBCB? {
        return Mockito.mock(PixChaveBCB::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStubRegistra(
            @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
        ): RegistrarChaveServiceGrpc.RegistrarChaveServiceBlockingStub? {
            return RegistrarChaveServiceGrpc.newBlockingStub(channel)
        }
    }
}

