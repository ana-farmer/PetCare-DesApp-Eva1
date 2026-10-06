package org.example

import kotlinx.coroutines.delay

class BoxNoDisponibleException(mensaje: String) : Exception(mensaje)
class TarifaInvalidaException(mensaje: String) : Exception(mensaje)

class PetCareSystem {

    val boxes: List<Box> = List(10) { indice -> Box(indice + 1) }
    val historial = mutableListOf<Ticket>()

    suspend fun ingresarPaciente(paciente: Paciente): Int {
        val boxLibre = boxes.firstOrNull { it.estado == EstadoBox.Libre }
            ?: throw BoxNoDisponibleException("No hay boxes libres en este momento para el paciente ${paciente.nombre}.")

        boxLibre.estado = EstadoBox.EnProceso("Registrando entrada")
        delay(3000)

        boxLibre.estado = EstadoBox.EnAtencion(paciente)
        return boxLibre.numero
    }

    suspend fun liberarBoxYCobrar(codigoPaciente: String, minutosUso: Int): Double {
        val box = boxes.find {
            val estadoActual = it.estado
            estadoActual is EstadoBox.EnAtencion && estadoActual.paciente.codigo == codigoPaciente
        } ?: throw Exception("Error: Paciente con código $codigoPaciente no encontrado en ningún box.")

        val paciente = (box.estado as EstadoBox.EnAtencion).paciente

        box.estado = EstadoBox.EnProceso("Calculando tarifa")
        delay(6500)

        val tarifaBase = paciente.calcularTarifaBase(minutosUso)
        var totalFinal = tarifaBase * 1.19

        if (paciente.tipoDueno == TipoDueno.MUNICIPAL) {
            totalFinal *= 0.5
        }

        if (totalFinal <= 0) {
            box.estado = EstadoBox.EnAtencion(paciente)
            throw TarifaInvalidaException("Error: La tarifa calculada no puede ser menor o igual a cero.")
        }

        historial.add(Ticket(paciente, minutosUso, totalFinal))
        box.estado = EstadoBox.Libre

        return totalFinal
    }
}
