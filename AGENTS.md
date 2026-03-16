# Backend Conventions

## Arquitectura
- Mantener este backend como ejemplo docente deliberadamente simple: entidad JPA, repositorio Spring Data, controlador REST manual y poco mas.
- Convivir con dos superficies HTTP: API manual bajo `/biblioteca` y Spring Data REST bajo `/api`.
- No introducir capa de servicio mientras las reglas sigan siendo triviales y no aporte claridad didactica.

## Modelo de obra
- `Obra` es la entidad principal del catalogo y contiene solo metadatos sencillos mas una copia digital opcional.
- La copia digital se almacena en la base de datos como `@Lob` y nunca debe exponerse en JSON.
- La URL de descarga debe derivarse del identificador y publicarse como propiedad de solo lectura.
- La `fechaDeposito` es metadato de salida: la fija el servidor al subir o reemplazar el PDF.

## Publicacion y descarga
- El alta de una obra y la subida del fichero son pasos separados.
- `POST /biblioteca/obras` acepta solo metadatos; usar DTO especifico para no permitir campos internos o calculados.
- `PUT /biblioteca/obras/{id}/digital` consume `application/pdf`, es idempotente y reemplaza el binario existente.
- `GET /biblioteca/obras/{id}/digital` debe responder `404` tanto si la obra no existe como si no tiene copia digital.

## Persistencia y datos demo
- Mantener H2 en memoria como configuracion por defecto y dejar comentada una opcion de fichero local.
- Precargar varias obras de ejemplo al arrancar para que el proyecto sea util en clase sin pasos previos.
- Incluir al menos un PDF real en `src/main/resources/documents/` para poder demostrar la descarga end-to-end.

## Lombok y estilo
- Usar Lombok para getters, setters y constructores cuando solo elimine ruido.
- No usar `@Data` en entidades JPA.
- Preferir nombres y estructuras simples frente a abstracciones adicionales.

## Pruebas
- Cubrir consultas y filtro por autor con `@DataJpaTest`.
- Cubrir la API manual con `@WebMvcTest`, incluyendo alta, validacion, subida y descarga.
- Mantener un smoke test `@SpringBootTest` que verifique arranque, datos demo, Data REST y Swagger UI.
