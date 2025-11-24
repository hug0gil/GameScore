# GameScore

## **1. Descripción del Proyecto y Temática Elegida**

**GameZone** es una plataforma web moderna diseñada para entusiastas de los videojuegos. El objetivo principal del proyecto es ofrecer un catálogo centralizado de juegos, donde los usuarios pueden no solo consultar información detallada, sino también participar activamente creando reseñas, llevando un registro de sus logros personales y descubriendo nuevos títulos.

## **2. Lista de funcionalidades implementadas**
El proyecto se divide en tres módulos funcionales principales:
    I. Módulo Público y Catálogo (Acceso Abierto)
        - Página de Inicio (index.html): Portal de bienvenida con enlaces de acceso y un diseño retro.

        -Catálogo de Juegos (games.html): Listado navegable de videojuegos, incluyendo títulos destacados.

        -Detalle del Juego (game-detail.html): Información completa de un título y sección de reseñas publicadas.

        -Información Corporativa: Vistas de apoyo como Sobre-Nosotros.html y Politica-Privacidad.html.

    II. Módulo de Usuario (Rol: USER)
        -Registro y Login (register.html, login.html): Soporte para registro tradicional y autenticación social (SSO) vía OAuth 2.0.

        -Perfil Personal (profile.html): Visualización de datos, actividad, reseñas recientes y juegos favoritos.

        -Edición de Perfil (edit-profile.html): Permite al usuario actualizar su nombre, username y URL de avatar (con previsualización).

        -Formulario de Reseña (review-form.html): Crear o editar una reseña con título, contenido y puntuación de 1 a 5 estrellas.

        - Envío de Resumen de Actividad: Funcionalidad para que el usuario solicite el envío de un email con un resumen de sus estadísticas (summary-template.html).

    III. Módulo de Administración (Rol: ADMIN)
        Este módulo solo es accesible para usuarios con el rol ADMIN.

        -Panel Principal (dashboard.html): Vista de estadísticas clave (usuarios totales, nuevos, bajas, métricas de juegos y reseñas) con gráficos de seguimiento (Chart.js).

        -Gestión de Juegos (games.html):
            -Listado y búsqueda de juegos con detalles básicos.
            -Botón para Reestablecer Videojuegos (sincronización con API externa).
            -Formulario de Juego (game-form.html): Creación y edición de juegos, permitiendo modificar datos detallados como slug, rating, metacritic y URL de imágenes.

        -Gestión de Reseñas (reviews.html):
            -Listado completo de todas las reseñas de la plataforma.
            -Filtros Avanzados: Filtrado por estado (PENDING, APPROVED, REJECTED) y puntuación mínima.
            -Acciones de Moderación: Permite al administrador aprobar o rechazar las reseñas.
        -Gestión de Usuarios (users.html):
            -Listado y búsqueda de usuarios por nombre, username o email.
            - Edición de Roles: Permite modificar el rol del usuario (por ejemplo, ascender o descender a ADMIN).
            - Eliminación Segura: Funcionalidad para eliminar usuarios con modal de confirmación (Bootstrap Modal).

## **APIs utilizadas y justificación de su elección**
## **Instrucciones de instalación y configuración**
## **Credenciales de prueba para cada rol**
## **Capturas de pantalla de todas las vistas principales**

