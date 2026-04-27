# AGENTS.md

## Propósito
Este proyecto es un backend docente de Spring Boot para un curso de ingeniería de servicios web. Prima que el alumnado pueda leer, ejecutar, modificar y explicar el código sobre aplicar patrones arquitectónicos avanzados.

Las simplificaciones son intencionadas. Un agente de IA debe respetar el nivel técnico esperable del curso y ampliar el proyecto siguiendo el estilo ya existente.

## Reglas para agentes de IA
- Antes de proponer o modificar código, revisa el patrón actual del proyecto y elige la solución más pequeña que resuelva el problema.
- Este repositorio es el backend del sistema. Un cambio funcional en backend puede requerir cambios coordinados en el proyecto hermano de frontend, aunque su ruta concreta varíe según el entorno del alumno.
- No introduzcas dependencias, frameworks, capas o patrones avanzados sin que el enunciado lo pida de forma explícita o sin justificarlo antes.
- Si una mejora requiere un salto técnico importante, pregúntalo o explícalo como alternativa, no lo apliques por defecto.
- Mantén el código didáctico: nombres claros, flujo directo, comentarios breves solo cuando ayuden a entender una decisión y pruebas centradas en el comportamiento.
- No elimines anotaciones, funciones o clases ya existentes solo porque no encajen del todo con estas pautas. Conserva lo preexistente salvo que sea incorrecto, rompa el comportamiento o impida implementar bien la característica pedida.
- Usa `Obra` como ejemplo del estado actual, no como límite del modelo. El dominio puede crecer con nuevas entidades y relaciones cuando la nueva característica lo requiera.
- Si implementas una historia de usuario o una feature descrita a nivel funcional, antes de escribir código cierra el comportamiento observable que falta por concretar: contratos públicos, endpoints, payloads, validaciones, códigos de respuesta y persistencia afectada.
- Cuando el enunciado de una historia de usuario no fije esos detalles, dedúcelos a partir del código existente, las pruebas, `docs/current-contracts.md` y el estilo del proyecto, eligiendo siempre la solución más pequeña y coherente.
- Si una feature deja de ser coherente sin cambios en el frontend, no des por terminada la tarea en backend sin señalar esa dependencia y sin contemplar también los ajustes necesarios en el proyecto hermano.
- Distingue entre reglas generales del proyecto y contratos concretos ya implementados. Los contratos actuales deben respetarse mientras no se cambie explícitamente el enunciado.
- Si modificas un contrato público ya implementado, actualiza también `docs/current-contracts.md`.

## Capacidades esperadas
- Java 17 y Spring Boot MVC.
- Spring Data JPA para entidades, repositorios y relaciones sencillas.
- Spring Data REST bajo `/api` cuando interese mostrar exposición automática de repositorios.
- Controladores REST manuales para operaciones que necesiten contrato HTTP explícito, validación clara, cuerpos binarios o respuestas personalizadas.
- H2 como base de datos de desarrollo y pruebas, con datos demo precargados cuando faciliten el trabajo en clase.
- Bean Validation para reglas de entrada simples.
- springdoc/OpenAPI para documentar y probar la API desde Swagger UI.
- Lombok para reducir ruido en getters, setters, constructores y builders cuando no oculte comportamiento relevante.

## Arquitectura docente
- Mantén una estructura simple basada en entidades JPA, repositorios Spring Data, controladores REST, configuración y carga de datos demo.
- Puedes añadir nuevas entidades, repositorios, validaciones y relaciones JPA si el dominio lo requiere.
- No añadas una capa de servicio por defecto. Introdúcela solo cuando la lógica de negocio compartida deje de ser razonable en el controlador del agregado, haya reutilización real en varios puntos de los servicios REST o la coordinación necesaria deje de ser didácticamente clara sin esa capa. Aunque una transacción afecte a varias entidades, si la operación sigue siendo clara y didáctica dentro del controlador del agregado, resuélvela ahí antes de crear un `Service`.
- No conviertas el proyecto a arquitectura hexagonal, CQRS, eventos de dominio, WebFlux, seguridad real, colas, cachés, Docker o migraciones de base de datos salvo petición explícita.
- Mantén la convivencia entre API manual y Spring Data REST cuando aporte valor docente. Evita que ambas superficies se contradigan.

## Contratos y DTOs
- Las entidades JPA pueden formar parte del contrato JSON cuando el caso sea sencillo y el resultado sea comprensible para clase.
- Usa DTOs solo cuando ayuden a proteger o aclarar el contrato: evitar campos internos o calculados en la entrada, ocultar binarios, separar respuestas de escritura, simplificar relaciones o representar una operación que no encaja bien con la entidad.
- No impongas DTOs para todos los endpoints por sistema.
- Sigue un principio de robustez moderado en la entrada JSON: no añadas por defecto guardas o configuraciones de Jackson para rechazar campos desconocidos u otras variaciones menores del payload si no son necesarias para la característica pedida.
- Los campos internos, binarios o calculados no deben exponerse ni aceptar escritura accidental desde JSON.
- Las respuestas de error deben ser simples y útiles para quien consume la API.
- Para errores sencillos de controladores manuales, usa `ResponseStatusException` directamente en el controlador con el código HTTP y un mensaje claro. No crees excepciones personalizadas ni un `@ControllerAdvice` si solo trasladan un caso puntual sin simplificar el aprendizaje.
- Considera excepciones o un manejador global solo cuando varios controladores compartan el mismo tratamiento de errores o cuando la duplicación empiece a dificultar la lectura.
- La documentación detallada de los contratos públicos vigentes vive en `docs/current-contracts.md`.

## Diseño REST
- Diseña la API manual en torno a recursos del dominio, colecciones, subrecursos o registros de eventos, no como una lista de comandos RPC.
- Usa verbos HTTP con su significado habitual: `GET` para consultar, `POST` para crear recursos o registrar eventos, `PUT` para reemplazos idempotentes y `DELETE` solo cuando el dominio permita eliminar.
- Cuando una operación del dominio cambie el estado de forma incremental, como registrar una entrada o salida, prefiere modelarla como creación de un recurso o evento asociado al agregado antes que exponer un endpoint genérico que sobrescriba campos calculados o cantidades arbitrarias.
- Los nombres de rutas deben sonar al lenguaje del dominio y ser comprensibles para clase. Evita nombres técnicos o acciones verbales innecesarias si puede expresarse como recurso.
- Mantén los códigos de respuesta HTTP simples y consistentes con el controlador existente: `201 Created` con `Location` cuando se crea un recurso, `200 OK` cuando se devuelve el estado actualizado, `404 Not Found` para recursos inexistentes, `400 Bad Request` para entradas inválidas y `409 Conflict` para inconsistencias de estado.
- No compliques la API con hipermedia avanzada, versionado, negociación compleja de contenidos o patrones REST sofisticados salvo petición explícita.

## Persistencia y datos demo
- Mantén H2 en memoria como configuración por defecto y conserva una alternativa comentada de fichero local si ya existe.
- Usa `ddl-auto=create` mientras el objetivo sea mostrar entidades y relaciones sin introducir migraciones.
- Precarga datos suficientes para que la aplicación sea demostrable sin pasos previos.
- Si se gestionan ficheros o binarios, conserva el enfoque simple de almacenarlos y servirlos de forma explícita, sin sistemas externos.
- Al modelar persistencia, prefiere las convenciones por defecto de JPA/Hibernate antes que anotar nombres de tablas, columnas o joins de forma explícita. Evita `@Table`, `@JoinColumn` y similares (salvo cuando sean realmente necesarios para la característica pedida).

## Estilo
- Preferir nombres y estructuras directas frente a abstracciones genéricas.
- Usar Lombok para getters, setters, constructores y builders cuando solo elimine ruido.
- No usar `@Data` en entidades JPA.
- Evitar utilidades globales, mapeadores genéricos o jerarquías de clases si no reducen una duplicación real.
- Mantener la configuración en `application.properties` de forma visible y comentada cuando tenga valor docente.

## Pruebas
- Cubrir repositorios y consultas relevantes con `@DataJpaTest`.
- Cubrir controladores manuales con `@WebMvcTest`, incluyendo validación, errores y contratos HTTP importantes.
- Mantener un smoke test `@SpringBootTest` que verifique arranque, datos demo, Data REST y Swagger UI cuando aplique.
- Cuando se añadan entidades o relaciones, probar al menos el flujo feliz y un caso de validación o error significativo.
