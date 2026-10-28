# VITAIROS-M · Experiencia móvil de orientación y acompañamiento

Proyecto de la **Evaluación Parcial 2 (DSY1105 – Desarrollo de Aplicaciones Móviles)**, basado en el caso VITAIROS SpA (CITT Duoc UC Sede Valparaíso).

> MVP académico. Usa únicamente datos ficticios o sintéticos. No entrega diagnósticos ni reemplaza la atención profesional.

## Integrantes
- _Nombre Apellido_ (equipo N°_)
- _Nombre Apellido_

## Funcionalidades
- **Onboarding** con aviso de límites (orientación, no diagnóstico) y **login / creación de perfil** simulado (contraseña con hash SHA-256 + sal).
- **Inicio**: próximo paso sugerido, gráfico de 7 días, racha y accesos rápidos.
- **Registro emocional** validado (ánimo, área, nota) con detección de señales de riesgo que muestra líneas de ayuda.
- **Bitácora**: historial con filtro por área, trazabilidad (fecha, hora, ID), eliminación con confirmación y compartir.
- **Orientación**: chat conversacional simulado que persiste entre sesiones y sugiere próximos pasos.
- **Agenda**: profesionales ficticios, disponibilidad, validación de choques de horario, estados Pendiente → Confirmada / Cancelada.
- **Recursos** informativos de prueba, ejercicio de respiración animado y líneas de ayuda.
- **Perfil y ajustes**: modo oscuro, recordatorios, hora validada, cargar/borrar datos sintéticos.

## Cumplimiento de la rúbrica
| Indicador | Dónde |
|---|---|
| IE 2.1.1 Interfaz y navegación | `ui/theme`, `ui/screen`, `navigation/AppNavigation.kt` (Material 3, barra inferior, transiciones) |
| IE 2.1.2 Formularios validados | 6 formularios: login, crear perfil, registro emocional, cita, hora de recordatorio, chat. Componente `CampoValidado` (íconos de error/éxito y mensajes) |
| IE 2.2.1 Validación desacoplada | `validacion/Validadores.kt` (lógica pura) + `StateFlow` en los ViewModel |
| IE 2.2.2 Animaciones | selector de ánimo, barras del gráfico, transiciones de navegación, indicador "escribiendo", respiración, tarjetas expandibles, `animateItem` |
| IE 2.3.1 Modularidad y persistencia | MVVM por capas (`model`, `data`, `domain`, `viewmodel`, `ui`); DataStore con JSON en `data/` |
| IE 2.3.2 GitHub y Trello | ver `docs/PLAN_TRABAJO.md` |
| IE 2.4.1 Recursos nativos | Notificaciones (permiso en tiempo de ejecución), marcador telefónico, vibración y compartir |

## Arquitectura
```
cl.duoc.vitairosm
├── model/        modelos y catálogos
├── validacion/   reglas de validación (sin dependencias de Android)
├── domain/       disponibilidad y motor de orientación
├── data/         DataStore + serialización JSON + datos sintéticos
├── util/         notificaciones, dispositivo, seguridad, formato
├── viewmodel/    estado de cada flujo
├── ui/           tema, componentes y pantallas (Compose + Material 3)
└── navigation/   rutas y NavHost
```

## Ejecutar
1. Abrir la carpeta en Android Studio (JDK/Gradle según `gradle/`).
2. Sincronizar Gradle y ejecutar en un emulador o teléfono Android 8.0+ (API 26).
3. En el primer uso: aceptar el aviso → *Crear perfil de prueba* → en Inicio, *Cargar datos sintéticos de ejemplo*.
4. Pruebas unitarias: `./gradlew test`.

## Pendiente para la EFT
API REST/mocks para autenticación y agenda, base de datos Room, recordatorios programados (WorkManager), accesibilidad (TalkBack) y vista del profesional.
