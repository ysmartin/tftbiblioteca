# AGENTS.md

## Proposito
Este proyecto es un backend docente de Spring Boot para un curso de ingenieria de servicios web. Prima que el alumnado pueda leer, ejecutar, modificar y explicar el codigo sobre aplicar patrones arquitectonicos avanzados.

Las simplificaciones son intencionadas. Un agente de IA debe respetar el nivel tecnico esperable del curso y ampliar el proyecto siguiendo el estilo ya existente.

## Reglas para agentes de IA
- Antes de proponer o modificar codigo, revisa el patron actual del proyecto y elige la solucion mas pequena que resuelva el problema.
- Este repositorio es el backend del sistema. Un cambio funcional en backend puede requerir cambios coordinados en el proyecto hermano de frontend, aunque su ruta concreta varie segun el entorno del alumno.
- No introduzcas dependencias, frameworks, capas o patrones avanzados sin que el enunciado lo pida de forma explicita o sin justificarlo antes.
- Si una mejora requiere un salto tecnico importante, preguntalo o explicalo como alternativa, no lo apliques por defecto.
- Manten el codigo didactico: nombres claros, flujo directo, comentarios breves solo cuando ayuden a entender una decision y pruebas centradas en el comportamiento.
- No elimines anotaciones, funciones o clases ya existentes solo porque no encajen del todo con estas pautas. Conserva lo preexistente salvo que sea incorrecto, rompa el comportamiento o impida implementar bien la caracteristica pedida.
- Usa `Obra` como ejemplo del estado actual, no como limite del modelo. El dominio puede crecer con nuevas entidades y relaciones cuando la nueva característica lo requiera.
- Si implementas una historia de usuario o una feature descrita a nivel funcional, antes de escribir codigo cierra el comportamiento observable que falta por concretar: contratos publicos, endpoints, payloads, validaciones, codigos de respuesta y persistencia afectada.
- Cuando el enunciado de una historia de usuario no fije esos detalles, deducelos a partir del codigo existente, las pruebas, `docs/current-contracts.md` y el estilo del proyecto, eligiendo siempre la solucion mas pequena y coherente.
- Si una feature deja de ser coherente sin cambios en el frontend, no des por terminada la tarea en backend sin senalar esa dependencia y sin contemplar tambien los ajustes necesarios en el proyecto hermano.
- Distingue entre reglas generales del proyecto y contratos concretos ya implementados. Los contratos actuales deben respetarse mientras no se cambie explicitamente el enunciado.
- Si modificas un contrato publico ya implementado, actualiza tambien `docs/current-contracts.md`.

## Capacidades esperadas
- Java 17 y Spring Boot MVC.
- Spring Data JPA para entidades, repositorios y relaciones sencillas.
- Spring Data REST bajo `/api` cuando interese mostrar exposicion automatica de repositorios.
- Controladores REST manuales para operaciones que necesiten contrato HTTP explicito, validacion clara, cuerpos binarios o respuestas personalizadas.
- H2 como base de datos de desarrollo y pruebas, con datos demo precargados cuando faciliten el trabajo en clase.
- Bean Validation para reglas de entrada simples.
- springdoc/OpenAPI para documentar y probar la API desde Swagger UI.
- Lombok para reducir ruido en getters, setters, constructores y builders cuando no oculte comportamiento relevante.

## Arquitectura docente
- Manten una estructura simple basada en entidades JPA, repositorios Spring Data, controladores REST, configuracion y carga de datos demo.
- Puedes anadir nuevas entidades, repositorios, validaciones y relaciones JPA si el dominio lo requiere.
- No anadas una capa de servicio por defecto. Introducela solo cuando la logica de negocio compartida deje de ser razonable en el controlador del agregado, haya reutilizacion real en varios puntos de los servicios REST o la coordinacion necesaria deje de ser didacticamente clara sin esa capa. Aunque una transaccion afecte a varias entidades, si la operacion sigue siendo clara y didactica dentro del controlador del agregado, resuelvela ahi antes de crear un `Service`.
- No conviertas el proyecto a arquitectura hexagonal, CQRS, eventos de dominio, WebFlux, seguridad real, colas, caches, Docker o migraciones de base de datos salvo peticion explicita.
- Manten la convivencia entre API manual y Spring Data REST cuando aporte valor docente. Evita que ambas superficies se contradigan.

## Contratos y DTOs
- Las entidades JPA pueden formar parte del contrato JSON cuando el caso sea sencillo y el resultado sea comprensible para clase.
- Usa DTOs solo cuando ayuden a proteger o aclarar el contrato: evitar campos internos o calculados en la entrada, ocultar binarios, separar respuestas de escritura, simplificar relaciones o representar una operacion que no encaja bien con la entidad.
- No impongas DTOs para todos los endpoints por sistema.
- Sigue un principio de robustez moderado en la entrada JSON: no anadas por defecto guardas o configuraciones de Jackson para rechazar campos desconocidos u otras variaciones menores del payload si no son necesarias para la característica pedida.
- Los campos internos, binarios o calculados no deben exponerse ni aceptar escritura accidental desde JSON.
- Las respuestas de error deben ser simples y utiles para quien consume la API.
- La documentacion detallada de los contratos publicos vigentes vive en `docs/current-contracts.md`.

## Persistencia y datos demo
- Manten H2 en memoria como configuracion por defecto y conserva una alternativa comentada de fichero local si ya existe.
- Usa `ddl-auto=create` mientras el objetivo sea mostrar entidades y relaciones sin introducir migraciones.
- Precarga datos suficientes para que la aplicacion sea demostrable sin pasos previos.
- Si se gestionan ficheros o binarios, conserva el enfoque simple de almacenarlos y servirlos de forma explicita, sin sistemas externos.
- Al modelar persistencia, prefiere las convenciones por defecto de JPA/Hibernate antes que anotar nombres de tablas, columnas o joins de forma explicita. Evita `@Table`, `@JoinColumn` y similares (salvo cuando sean realmente necesarios para la característica pedid).

## Estilo
- Preferir nombres y estructuras directas frente a abstracciones genericas.
- Usar Lombok para getters, setters, constructores y builders cuando solo elimine ruido.
- No usar `@Data` en entidades JPA.
- Evitar utilidades globales, mapeadores genericos o jerarquias de clases si no reducen una duplicacion real.
- Mantener la configuracion en `application.properties` de forma visible y comentada cuando tenga valor docente.

## Pruebas
- Cubrir repositorios y consultas relevantes con `@DataJpaTest`.
- Cubrir controladores manuales con `@WebMvcTest`, incluyendo validacion, errores y contratos HTTP importantes.
- Mantener un smoke test `@SpringBootTest` que verifique arranque, datos demo, Data REST y Swagger UI cuando aplique.
- Cuando se anadan entidades o relaciones, probar al menos el flujo feliz y un caso de validacion o error significativo.
