package org.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Scanner

fun main() = runBlocking {
    val sistema = PetCareSystem()
    val scanner = Scanner(System.`in`)
    var continuar = true
    val moneda = "$"

    println("======================================")
    println("        BIENVENIDO A PETCARE          ")
    println("======================================")

    while (continuar) {
        println("\n--- MENÚ ---")
        println("1. Ingresar mascota a un box")
        println("2. Realizar examen médico")
        println("3. Dar de alta y realizar cobro")
        println("4. Ver estado de los boxes")
        println("5. Salir y generar reporte de cierre")
        print("Seleccione una opción: ")

        when (scanner.nextLine().trim()) {
            "1" -> {
                println("\n--- INGRESO DE MASCOTA ---")

                print("Código de atención (ej. CA12CD): ")
                val codigo = scanner.nextLine().trim().uppercase()

                if (!Validador.validarCodigoAtencion(codigo)) {
                    println("ERROR: El código de atención no cumple el formato válido (2 letras, 2 números, 2 letras).")
                    continue
                }

                print("Nombre de la mascota: ")
                val nombre = scanner.nextLine().trim()

                print("Tipo (1: Canino, 2: Felino, 3: Exótico): ")
                val tipo = scanner.nextLine().trim()

                print("Dueño (1: Particular, 2: Convenio, 3: Municipal): ")
                val tipoDuenoInput = scanner.nextLine().trim()

                val tipoDueno = when (tipoDuenoInput) {
                    "2" -> TipoDueno.CONVENIO
                    "3" -> TipoDueno.MUNICIPAL
                    else -> TipoDueno.PARTICULAR
                }

                val paciente = when (tipo) {
                    "1" -> Canino(codigo, nombre, tipoDueno)
                    "2" -> Felino(codigo, nombre, tipoDueno)
                    "3" -> {
                        print("¿Es silvestre? (s/n): ")
                        val esSilvestre = scanner.nextLine().trim().lowercase() == "s"
                        Exotico(codigo, nombre, tipoDueno, esSilvestre)
                    }
                    else -> Canino(codigo, nombre, tipoDueno)
                }

                launch(Dispatchers.Default) {
                    try {
                        println("\n[SISTEMA]: Enviando solicitud al sensor para ingreso...")
                        val numeroBox = sistema.ingresarPaciente(paciente)
                        println("\n[SENSOR]: ¡Ingreso exitoso! $nombre ha sido ingresado al Box$numeroBox.")
                    } catch (e: Exception) {
                        println("\n[ERROR DE SENSOR]: ${e.message}")
                    }
                }
            }

            "2" -> {
                print("\nIngrese el número del box para el examen: ")
                val boxNum = scanner.nextLine().trim().toIntOrNull()

                if (boxNum != null) {
                    val box = sistema.boxes.find { it.numero == boxNum }
                    when (box?.estado) {
                        is EstadoBox.EnAtencion -> {
                            print("Ingrese el nombre del examen (ej. rx, hemograma): ")
                            val examen = scanner.nextLine().trim()

                            launch(Dispatchers.Default) {
                                val estadoAnterior = box.estado
                                box.estado = EstadoBox.EnProceso(examen)
                                println("\n[SISTEMA]: Paciente en el Box $boxNum preparándose para examen: $examen...")
                                delay(15000)
                                box.estado = estadoAnterior
                                println("\n[SENSOR]: ¡Atención! El examen de $examen en el Box$boxNum ha finalizado exitosamente.")
                            }
                        }
                        is EstadoBox.Libre -> println("ERROR: El box $boxNum está vacío. Debes ingresar un paciente primero (Opción 1).")
                        is EstadoBox.EnProceso -> println("ERROR: El box $boxNum está ocupado procesando datos. Espera unos segundos.")
                        null -> println("ERROR: El box $boxNum no existe (solo hay del 1 al 10).")
                        else -> println("ERROR: El box no está disponible para exámenes en este momento.")
                    }
                } else {
                    println("ERROR: Número de box inválido. Ingresa solo el número.")
                }
            }

            "3" -> {
                print("\nIngrese el CÓDIGO DE ATENCIÓN del paciente a dar de alta: ")
                val codigoPaciente = scanner.nextLine().trim().uppercase()

                print("Tiempo de uso de box en minutos: ")
                val minutos = scanner.nextLine().trim().toIntOrNull()

                if (minutos != null) {
                    launch(Dispatchers.Default) {
                        try {
                            println("\n[SISTEMA]: Procesando salida y calculando tarifa. Por favor espere...")
                            val total = sistema.liberarBoxYCobrar(codigoPaciente, minutos)
                            println("\n[CAJA]: Paciente $codigoPaciente dado de alta. Total a pagar: $moneda${total.toInt()}")
                        } catch (e: Exception) {
                            println("\n[ERROR EN CAJA]: ${e.message}")
                        }
                    }
                } else {
                    println("ERROR: Datos inválidos. Ingrese un tiempo en minutos válido.")
                }
            }

            "4" -> {
                println("\n--- ESTADO DE LOS BOXES ---")
                sistema.boxes.forEach { box ->
                    val estado = when (val e = box.estado) {
                        is EstadoBox.Libre -> "Libre"
                        is EstadoBox.EnAtencion -> "Ocupado por ${e.paciente.nombre} (${e.paciente.especie})"
                        is EstadoBox.EnProceso -> "Procesando: ${e.motivo}"
                        is EstadoBox.FueraDeServicio -> "Inhabilitado"
                    }
                    println("Box ${box.numero}:$estado")
                }
            }

            "5" -> {
                println("\n======================================")
                println("       REPORTE DE CIERRE DE TURNO       ")
                println("======================================")

                val disponibles = sistema.boxes.count { it.estado == EstadoBox.Libre }
                val pacientesConvenio = sistema.historial.filter { it.paciente.tipoDueno == TipoDueno.CONVENIO }
                val recaudacionTotal = sistema.historial.sumOf { it.totalPagado }
                val ingresosPromedio = if (sistema.historial.isNotEmpty()) recaudacionTotal / sistema.historial.size else 0.0
                val codigosFinalizados = sistema.historial.map { it.paciente.codigo }
                val pacienteMayorTiempo = sistema.historial.maxByOrNull { it.minutosUso }

                println("Detalle de Pacientes Atendidos:")
                sistema.historial.forEachIndexed { index, ticket ->
                    println("Ticket #${index + 1} | Cod: ${ticket.paciente.codigo} | Tipo: ${ticket.paciente.especie} | Tiempo: ${ticket.minutosUso} min | Pagado: $moneda${ticket.totalPagado.toInt()}")

                }

                println("\nConsultas de Negocio:")
                println("- Boxes disponibles actualmente: $disponibles")
                println("- Cantidad de pacientes de convenio atendidos: ${pacientesConvenio.size}")
                println("- Ingreso promedio por paciente: $moneda${ingresosPromedio.toInt()}")
                println("- Códigos de pacientes finalizados: $codigosFinalizados")
                println("- Paciente con mayor tiempo de uso: ${pacienteMayorTiempo?.paciente?.codigo ?: "Ninguno"} con ${pacienteMayorTiempo?.minutosUso ?: 0} minutos")
                println("- Total general recaudado: $moneda${recaudacionTotal.toInt()}")

                println("\n¡Gracias por usar PetCare!")
                continuar = false
            }

            else -> println("ERROR: Opción no válida, ingrese una opción válida.")
        }
    }
}