package org.example

enum class TipoDueno {
    PARTICULAR, CONVENIO, MUNICIPAL
}

sealed class EstadoBox {
    data object Libre : EstadoBox()
    data class EnAtencion(val paciente: Paciente) : EstadoBox()
    data class EnProceso(val motivo: String) : EstadoBox()
    data class FueraDeServicio(val motivo: String) : EstadoBox()
}

open class Paciente(
    val codigo: String,
    val nombre: String,
    val especie: String,
    val tipoDueno: TipoDueno,
    val horaIngreso: Long = System.currentTimeMillis()
) {
    open fun calcularTarifaBase(minutosUso: Int): Double {
        return 0.0
    }
}

class Canino(codigo: String, nombre: String, tipoDueno: TipoDueno) :
    Paciente(codigo, nombre, "Canino", tipoDueno) {

    override fun calcularTarifaBase(minutosUso: Int): Double {
        val tarifaHora = 12000.0
        val minutosFacturables = if (tipoDueno == TipoDueno.CONVENIO) minutosUso * 0.8 else minutosUso.toDouble()
        return (minutosFacturables / 60.0) * tarifaHora
    }
}

class Felino(codigo: String, nombre: String, tipoDueno: TipoDueno) :
    Paciente(codigo, nombre, "Felino", tipoDueno) {

    override fun calcularTarifaBase(minutosUso: Int): Double {
        if (minutosUso < 20) return 0.0
        val tarifaHora = 9000.0
        return (minutosUso / 60.0) * tarifaHora
    }
}

class Exotico(codigo: String, nombre: String, tipoDueno: TipoDueno, val esSilvestre: Boolean) :
    Paciente(codigo, nombre, "Exótico", tipoDueno) {

    override fun calcularTarifaBase(minutosUso: Int): Double {
        val tarifaHora = 20000.0
        var totalBase = (minutosUso / 60.0) * tarifaHora
        if (esSilvestre) {
            totalBase *= 1.30
        }
        return totalBase
    }
}

class Box(val numero: Int) {
    var estado: EstadoBox = EstadoBox.Libre
}

data class Ticket(
    val paciente: Paciente,
    val minutosUso: Int,
    val totalPagado: Double
)