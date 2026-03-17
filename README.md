# tftbiblioteca

Backend docente muy simple en Spring Boot: expone un catálogo de obras con dos superficies HTTP simultáneas. La API manual vive bajo `/biblioteca/obras` y la exposición automática de Spring Data REST bajo `/api/obras`. Los datos de ejemplo se cargan al arrancar para poder probar sin pasos previos.

## Ejecutar en local
- Requisitos: Java 17+ y Maven.
- Desde la carpeta `tftbiblioteca`: `mvn spring-boot:run`.
- La aplicación escucha en `http://localhost:8070` (configurable en `application.properties`).

## Rutas útiles
- API manual: `GET/POST /biblioteca/obras`, `GET/PUT /biblioteca/obras/{id}/digital` (PDF).
- Spring Data REST: `GET /api/obras`, búsquedas en `/api/obras/search`.
- Documentación: `GET /swagger-ui/index.html` y `GET /v3/api-docs`.
- Consola H2: `GET /h2-console` (driver `org.h2.Driver`, JDBC `jdbc:h2:mem:tftbiblioteca`, usuario `sa`, sin contraseña).

## Notas para clase
- El esquema se crea en cada arranque (`spring.jpa.hibernate.ddl-auto=create`) para ver el efecto de las entidades sin migraciones.
- El PDF de demo está en `src/main/resources/documents/obra-demo.pdf` y se reutiliza para mostrar la subida/descarga.
