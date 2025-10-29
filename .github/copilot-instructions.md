<!-- Instrucciones para agentes AI que trabajan en este repositorio -->
# Guía rápida para agentes en Proyecto-FInal-Avance-2

Propósito: ayudar a agentes a ser productivos inmediatamente leyendo estructura, convenciones y puntos críticos del proyecto Android (Kotlin + Jetpack Compose).

1) Contexto alto nivel
- Proyecto: app Android (module `app`) - aplicación de notas usando Jetpack Compose.
- Principales componentes: UI (Compose), Room (base de datos), BroadcastReceiver para notificaciones, FileProvider para multimedia.
- Rutas clave:
  - `app/src/main/java/com/example/notesapp_apv_czg/MainActivity.kt` — entrada principal y creación de canal de notificaciones.
  - `app/src/main/java/com/example/notesapp_apv_czg/broadcastreceivers/NotificationReceiver.kt` — receptor para notificaciones.
  - `app/src/main/java/com/example/notesapp_apv_czg/data/` — entidades (`Note.kt`, `Multimedia.kt`, `Notification.kt`), DAOs y `AppDatabase.kt`.
  - `app/src/main/AndroidManifest.xml` — permisos y provider (ver `applicationId` interpolation).

2) Comandos de build / prueba / debug (Windows PowerShell)
- Compilar (rápido): `.
  gradlew.bat assembleDebug` (desde la raíz del repo)
- Compilar limpio y forzar kapt/regeneración de stubs: `.
  gradlew.bat clean assembleDebug`
- Tests de unidad: `.
  gradlew.bat test` — revisa `app/src/test`.
- Tests instrumentados / en dispositivo: `.
  gradlew.bat connectedAndroidTest`
- Usar Android Studio para debugging y emulador; permisos relevantes están en `app/src/main/AndroidManifest.xml`.

3) Convenciones del proyecto
- Catálogo de dependencias gestionado por `gradle/libs.versions.toml` y accesible vía `libs` (ver `app/build.gradle.kts`).
- Room: KAPT está habilitado (plugin `kapt`) — después de añadir Entity/DAO ejecutar `clean assembleDebug` para generar código.
- No cambiar `applicationId` sin actualizar provider authorities en `AndroidManifest.xml` (usa `${applicationId}.provider`).
- Package root: `com.example.notesapp_apv_czg` — ubica nuevas clases y paquetes dentro de esta jerarquía.

4) Integraciones y permisos críticas
- Permisos solicitados (ver `AndroidManifest.xml`): POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM, RECORD_AUDIO, CAMERA, READ_EXTERNAL_STORAGE.
- FileProvider configurado en `AndroidManifest.xml` y `res/xml/file_paths.xml` — cambiar rutas de archivos requiere actualizar ambos.
- Notificaciones: MainActivity arma Intents con extras (TITLE, DESCRIPTION, NOTIFICATION_ID) y usa `NotificationReceiver.CHANNEL_ID`.

5) Ejemplos de tareas concretas
- Añadir un nuevo campo a `Note`: actualizar `Note.kt`, migrar `AppDatabase` si se requiere versión nueva, y ejecutar `.
  gradlew.bat clean assembleDebug`.
- Añadir una pantalla Compose: crear archivo en `app/src/main/java/com/example/notesapp_apv_czg/ui/` y registrar navegación usando `navigation-compose`.

6) Pistas para el agente: cómo proponer cambios útiles
- Siempre referencia archivos afectados (ruta completa), e incluye comandos de compilación para verificar `kapt`/Room.
- Si modificas manifest/provider/IDs, menciona el impacto en firmas/authorities.
- Para cambios en dependencias, actualiza `gradle/libs.versions.toml` y `app/build.gradle.kts` (usar aliases donde sea posible).

7) Contacto y feedback
- Si algo no está claro en estas instrucciones, pregunta qué parte del flujo (build, DB, notificaciones) requiere más detalle.

Notas: Documenta solo cambios reales y comprobables en el repo; evita sugerir prácticas no visibles aquí.
