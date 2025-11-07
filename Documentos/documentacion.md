# **Documentación del Proyecto: GameZone**

![Logo del Proyecto Aquí](https://i.imgur.com/your-logo-placeholder.png "Inserta tu logo")

**Proyecto:** Portal de Videojuegos "GameZone"
**Autores:** [Tu Nombre / Nombres del Grupo]
**Fecha:** Noviembre, 2023
**Curso:** [Nombre del Curso/Materia]

---

## **Índice**

1.  [Introducción y Objetivos](#1-introducción-y-objetivos)
2.  [Arquitectura del Sistema](#2-arquitectura-del-sistema)
3.  [Tecnologías Utilizadas](#3-tecnologías-utilizadas)
4.  [Modelo de Datos (Esquema de BBDD)](#4-modelo-de-datos-esquema-de-bbdd)
5.  [Funcionalidades Implementadas](#5-funcionalidades-implementadas)
6.  [Documentación de la API REST](#6-documentación-de-la-api-rest-backend)
7.  [Integración de APIs Externas](#7-integración-de-apis-externas)
8.  [Guía de Instalación y Ejecución](#8-guía-de-instalación-y-ejecución)
9.  [Credenciales de Prueba](#9-credenciales-de-prueba)
10. [Posibles Mejoras Futuras](#10-posibles-mejoras-futuras)
11. [Anexo: Capturas de Pantalla](#11-anexo-capturas-de-pantalla)

---

## **1. Introducción y Objetivos**

**GameZone** es una plataforma web moderna diseñada para entusiastas de los videojuegos. El objetivo principal del proyecto es ofrecer un catálogo centralizado de juegos, donde los usuarios pueden no solo consultar información detallada, sino también participar activamente creando reseñas, llevando un registro de sus logros personales y descubriendo nuevos títulos.

El proyecto se construye sobre una arquitectura desacoplada de **Aplicación de Página Única (SPA)**, utilizando Angular para el frontend y una **API REST** robusta con Spring Boot para el backend, garantizando una experiencia de usuario fluida, interactiva y escalable.

Los objetivos clave son:
*   **Centralizar información:** Consumir datos de APIs externas para ofrecer un catálogo rico y actualizado.
*   **Fomentar la comunidad:** Permitir a los usuarios registrados compartir sus opiniones a través de reseñas.
*   **Personalización:** Ofrecer a cada usuario un dashboard personal para seguir su actividad y logros.
*   **Administración y Calidad:** Implementar un sistema de moderación de contenido y gestión de usuarios.
*   **Seguridad:** Garantizar la protección de datos y el acceso controlado a las funcionalidades mediante un sistema de roles y autenticación moderna (OAuth2 + JWT).

---

## **2. Arquitectura del Sistema**

El proyecto adopta una arquitectura de **Aplicación de Página Única (SPA) con una API RESTful**, separando completamente las responsabilidades del cliente (frontend) y del servidor (backend).

*   **Backend (Spring Boot):** Actúa como el cerebro de la aplicación. Es una API REST sin estado que gestiona la lógica de negocio, la seguridad, la persistencia de datos y la comunicación con servicios de terceros (RAWG, YouTube, SendGrid). No genera vistas HTML; su única salida es **JSON**.

*   **Frontend (Angular):** Es la cara visible de la aplicación. Se ejecuta completamente en el navegador del usuario y se encarga de la presentación de datos, la interacción y la experiencia de usuario. Se comunica con el backend a través de peticiones HTTP para obtener y enviar datos.

*   **Flujo de Autenticación (OAuth2 + JWT):**
    1.  El usuario inicia sesión con Google/GitHub desde el frontend de Angular.
    2.  Es redirigido al proveedor de OAuth2 para autenticarse.
    3.  Tras el éxito, el proveedor redirige al backend de Spring Boot.
    4.  El backend valida la información, crea/actualiza al usuario en la BBDD y genera un **JSON Web Token (JWT)**.
    5.  El backend redirige de vuelta al frontend, pasando el JWT como parámetro en la URL.
    6.  El frontend de Angular captura el JWT, lo almacena de forma segura (LocalStorage) y lo incluye en la cabecera de todas las peticiones a la API protegida.

```mermaid
graph TD
    subgraph Navegador del Usuario
        A[Frontend - Angular]
    end

    subgraph Servidor
        B[Backend - Spring Boot API]
        C[Base de Datos (PostgreSQL)]
    end

    subgraph APIs Externas
        D[RAWG API]
        E[YouTube API]
        F[SendGrid]
    end

    A -- Peticiones HTTP (JSON) --> B
    B -- Consultas SQL --> C
    B -- Peticiones API --> D
    B -- Peticiones API --> E
    B -- Peticiones API --> F