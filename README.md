# PetCare - Sistema de Gestion de Boxes Veterinarios

Aplicacion de consola desarrollada en Kotlin para la gestion de clinicas veterinarias, correspondiente a la asignatura DSY1105 - Desarrollo de Aplicaciones Moviles.

## Descripcion del Proyecto
Sistema desarrollado para controlar el ingreso y salida de pacientes, aplicar tarifas diferenciadas segun el tipo de mascota y dueño, gestionar el estado de los boxes mediante corrutinas asincronas, y generar reportes al cierre del turno.

## Tecnologias
* Lenguaje: Kotlin
* Entorno: IntelliJ IDEA
* Librerias: kotlinx-coroutines-core

## Estructura del Proyecto
* Main.kt: Menu principal e interaccion con el usuario.
* Modelos.kt: Clases de pacientes (Canino, Felino, Exotico), estados de boxes y enums.
* PetCareSystem.kt: Logica del sistema, calculo de tarifas y operaciones asincronas.
* Validador.kt: Validacion del formato del codigo de atencion.

## Funcionalidades
1. Gestion de Pacientes: Registro de Caninos, Felinos y Exoticos con tarifas y reglas especificas.
2. Control de Boxes: Manejo de estados (Libre, EnAtencion, EnProceso, FueraDeServicio).
3. Operaciones Asincronas: Uso de corrutinas para simular la comunicacion con sensores.
4. Reporte de Cierre: Resumen de ingresos, promedios y tiempo de uso al finalizar el turno.
5. Manejo de Errores: Control de excepciones para evitar fallos durante la ejecucion.

## Instrucciones de Ejecucion
1. Abrir el proyecto en IntelliJ IDEA.
2. Verificar las dependencias en build.gradle.kts.
3. Ejecutar el archivo Main.kt.