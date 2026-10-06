package org.example

object Validador {
    private val regexCodigo = Regex("""^[A-Za-z]{2}\d{2}[A-Za-z]{2}$""")

    fun validarCodigoAtencion(codigo: String): Boolean {
        return regexCodigo.matches(codigo)
    }
}