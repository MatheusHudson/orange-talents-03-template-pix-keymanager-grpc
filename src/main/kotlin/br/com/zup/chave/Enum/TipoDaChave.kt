package br.com.zup.chave.Enum

enum class TipoDaChave {

    CPF {
        override fun valida(chaveValor: String, createResponseError: (String) -> Unit) {
            if (!chaveValor.matches("^[0-9]{11}\$".toRegex()))
                return createResponseError("O cpf informado não corresponde com o formato: 12345678901 !")
        }
    },
    CELULAR {
        override fun valida(chaveValor: String, createResponseError: (String) -> Unit) {
            if (!chaveValor.matches("^\\([1-9]{2}\\) (?:[2-8]|9[1-9])[0-9]{3}\\-[0-9]{4}\$".toRegex()))
                return createResponseError("O celular informado não corresponde com o formato: (99) 98888-8888 ")

        }
    },
    EMAIL {
        override fun valida(chaveValor: String, createResponseError: (String) -> Unit) {
            if (!chaveValor.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$".toRegex()))
                return createResponseError("O email informado não corresponde com o formato: example@...")
        }
    },
    CHAVEALEATORIA {
        override fun valida(chaveValor: String, createResponseError: (String) -> Unit) {
            if (!chaveValor.isBlank())
                return createResponseError("Para uma chave aleatoria não insira um valor de chave!")
        }
    };


    companion object {
        val todasChavesString = "${TipoDaChave.values().map { tipoDaChave -> tipoDaChave.name }}".substringBeforeLast(", UNRECOGNIZED")
    }


    abstract fun valida(chaveValor: String, createResponseError: (String) -> Unit)
}