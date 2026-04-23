# Current Contracts

## Proposito
Este documento recoge los contratos publicos vigentes del proyecto. Debe actualizarse cuando cambien endpoints, payloads, campos de solo lectura, reglas de subida o descarga, o codigos de respuesta visibles para clientes.

El codigo y las pruebas siguen siendo la fuente de verdad ejecutable. Este fichero resume el contrato actual para humanos y para agentes de IA.

## Obra
- `Obra` es la entidad actual del catalogo.
- La API manual convive con Spring Data REST:
  - API manual bajo `/biblioteca/obras`
  - Data REST bajo `/api/obras`
- La respuesta JSON de `Obra` expone `id`, `autor`, `titulo`, `resumen`, `palabrasClave`, `fechaDeposito` y `urlCopiaDigital` cuando corresponda.
- `id`, `fechaDeposito` y `urlCopiaDigital` son campos de solo lectura para el cliente.
- La copia digital PDF no se expone en JSON.
- La URL de descarga se deriva del identificador y tiene la forma `/biblioteca/obras/{id}/digital`.

## Endpoints manuales vigentes

### `GET /biblioteca/obras`
- Lista todas las obras.
- Acepta opcionalmente el parametro de consulta `autor`.
- Si `autor` falta o llega en blanco, devuelve el listado completo.
- La respuesta se ordena por `id`.

### `GET /biblioteca/obras/{id}`
- Devuelve la obra indicada.
- Responde `404` si la obra no existe.

### `POST /biblioteca/obras`
- Crea una obra solo con metadatos.
- El cuerpo se valida con Bean Validation sobre `Obra`.
- El cliente no debe escribir campos internos, binarios o calculados.
- Responde `201 Created`.
- Incluye cabecera `Location` con la URL del nuevo recurso.

### `PUT /biblioteca/obras/{id}/digital`
- Sube o reemplaza la copia digital de una obra existente.
- Consume `application/pdf`.
- El cuerpo no puede estar vacio.
- La operacion actualiza `fechaDeposito` con la fecha del servidor.
- Responde `404` si la obra no existe.

### `GET /biblioteca/obras/{id}/digital`
- Descarga el PDF asociado a la obra.
- Produce `application/pdf`.
- Responde `404` si la obra no existe.
- Responde `404` si la obra existe pero no tiene copia digital.
- Devuelve la respuesta como descarga adjunta con nombre `obra-{id}.pdf`.

## Reglas de modelado actuales
- La copia digital se almacena en base de datos como `@Lob`.
- `palabrasClave` es una coleccion simple persistida como `@ElementCollection`.
- El contrato actual rechaza campos JSON desconocidos en `Obra`.
