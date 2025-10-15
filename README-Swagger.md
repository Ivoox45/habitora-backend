Swagger UI
==========

Después de iniciar la aplicación Spring Boot, Swagger UI estará disponible en:

- http://localhost:8080/swagger-ui.html
- ó http://localhost:8080/swagger-ui/index.html

La configuración de OpenAPI se encuentra en `src/main/java/com/habitora/backend/configuration/app/OpenApiConfig.java`.

Nota: Si usas seguridad (Spring Security), necesitas permitir el acceso a las rutas de OpenAPI (`/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`) en la configuración de seguridad.