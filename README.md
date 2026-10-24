# App de Estudio y Autoevaluación – ESFOCAR (MVP académico)

Asignatura: DSY1105 Desarrollo de Aplicaciones Móviles – Evaluación Parcial 2
Integrantes: _Nombre Apellido_ y _Nombre Apellido_ (Equipo N°__)

## Descripción
Aplicación Android para estudiantes en formación: permite estudiar por asignatura,
resolver trivias de autoevaluación con retroalimentación inmediata, guardar apuntes
personales y revisar el progreso. Funciona sin conexión (datos locales con Room).
Todos los datos son ficticios.

## Funcionalidades implementadas
- Vistas de registro, inicio y cierre de sesión (con confirmación), con formularios validados (íconos y mensajes por campo)
- Menú de tres puntos en la barra superior y barra de navegación inferior
- Carga, visualización y eliminación de documentos PDF y Word (selector de archivos del sistema)
- Trivias con retroalimentación inmediata y puntaje
- Formulario de apuntes personales (crear y eliminar) con validación
- Historial de resultados guardado en base de datos local
- Navegación con barra inferior (Estudiar, Trivia, Progreso)
- Animaciones: aparición de mensajes, cambio de color de respuestas, barras de progreso, cambio de pestañas
- Recursos nativos: archivos (PDF/Word), vibración y notificaciones (con solicitud de permiso)

## Arquitectura
MVVM + Repositorio + Room.
```
data/       base de datos (Room), repositorio y datos de ejemplo
model/      modelos del contenido
viewmodel/  estado y lógica de cada pantalla
ui/         pantallas (Jetpack Compose) y componentes reutilizables
util/       validaciones, seguridad, vibración, notificaciones
```

## Cómo ejecutar
1. Clonar el repositorio y abrirlo con Android Studio.
2. Esperar a que termine la sincronización de Gradle.
3. Ejecutar (Run) en un emulador o teléfono con Android 7.0 (API 24) o superior.
4. Crear una cuenta con un alias ficticio y una contraseña (mínimo 6 caracteres, con letras y números).
