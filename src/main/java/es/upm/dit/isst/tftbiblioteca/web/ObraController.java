package es.upm.dit.isst.tftbiblioteca.web;

import java.net.URI;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import es.upm.dit.isst.tftbiblioteca.domain.Obra;
import es.upm.dit.isst.tftbiblioteca.domain.ObraRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Controlador REST manual bajo <code>/biblioteca/obras</code>. Proporciona una
 * API (adicional a la generada automáticamente por Spring Data REST),
 * con control explícito sobre validación, cuerpos binarios y errores.
 */
@RestController
@RequestMapping("/biblioteca/obras") // evita confluir con la generada automáticamente por Data REST (/api/obras)
@Validated // activa validación en el controlador: en path params y cuerpos marcados con @Valid
public class ObraController {

    private final ObraRepository obraRepository;

    public ObraController(ObraRepository obraRepository) {
        this.obraRepository = obraRepository;
    }

    /**
     * Devuelve todas las obras, opcionalmente filtradas por autor.
     * La respuesta se ordena por id para que el consumo docente sea predecible.
     * URL por defecto (es decir, raíz indicada en @RequestMapping: GET /biblioteca/obras )
     */
    @GetMapping
    public List<Obra> listar(@RequestParam(name = "autor", required = false) String autor) {
        List<Obra> obras = (autor == null || autor.isBlank())
                ? StreamSupport.stream(obraRepository.findAll().spliterator(), false).toList()
                : obraRepository.findByAutorContainingIgnoreCase(autor);

        return obras.stream()
                .sorted(Comparator.comparing(Obra::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }

    /**
     * Recupera una obra concreta por id. Devuelve 404 si no existe para mantener
     * el contrato REST manual en lugar de la respuesta vacía de Data REST.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Obra> consultar(@PathVariable Long id) {
        return obraRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Obra no encontrada"));
    }

    /**
     * Alta de una obra solo con metadatos y respuesta con Location siguiendo
     * el patrón HTTP 201.
     */
    @PostMapping
    public ResponseEntity<Obra> crear(@Valid @RequestBody Obra obra) {
        Obra guardada = obraRepository.save(obra); // Necesario para recuperar el id de la obra guardada
        return ResponseEntity.created(URI.create("/biblioteca/obras/" + guardada.getId())).body(guardada);
    }

    /**
     * Sube o reemplaza el PDF asociado a una obra existente. Usa el cuerpo del
     * request como binario y actualiza la fecha de depósito en el dominio.
     */
    @PutMapping(path = "/{id}/digital", consumes = MediaType.APPLICATION_PDF_VALUE)
    @Operation( // documenta el cuerpo binario para poder hacer pruebas desde Swagger UI
            summary = "Sube o reemplaza el PDF asociado a una obra",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PDF_VALUE,
                            schema = @Schema(type = "string", format = "binary"))))
    public ResponseEntity<Obra> subirDigital(@PathVariable Long id, @RequestBody byte[] pdf) {
        if (pdf == null || pdf.length == 0) {
            throw new ResponseStatusException(BAD_REQUEST, "El PDF no puede estar vacio");
        }

        Obra obra = obraRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Obra no encontrada"));

        obra.reemplazarCopiaDigital(pdf, LocalDate.now());
        Obra actualizada = obraRepository.save(obra);
        return ResponseEntity.ok(actualizada);
    }

    /**
     * Devuelve el PDF almacenado para una obra. Si no existe obra o no hay
     * binario cargado, responde 404 para mantener el mismo contrato que el alta.
     */
    @GetMapping(path = "/{id}/digital", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Descarga el PDF asociado a una obra")
    public ResponseEntity<ByteArrayResource> descargarDigital(@PathVariable Long id) {
        Obra obra = obraRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Obra no encontrada"));

        if (!obra.tieneCopiaDigital()) {
            throw new ResponseStatusException(NOT_FOUND, "La obra no tiene copia digital");
        }

        ByteArrayResource resource = new ByteArrayResource(obra.getCopiaDigitalPdf());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"obra-" + id + ".pdf\"")
                .contentLength(obra.getCopiaDigitalPdf().length)
                .body(resource);
    }
}
