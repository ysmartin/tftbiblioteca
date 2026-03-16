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

@RestController
@RequestMapping("/biblioteca/obras")
@Validated
public class ObraController {

    private final ObraRepository obraRepository;

    public ObraController(ObraRepository obraRepository) {
        this.obraRepository = obraRepository;
    }

    @GetMapping
    public List<Obra> listar(@RequestParam(name = "autor", required = false) String autor) {
        List<Obra> obras = (autor == null || autor.isBlank())
                ? StreamSupport.stream(obraRepository.findAll().spliterator(), false).toList()
                : obraRepository.findByAutorContainingIgnoreCase(autor);

        return obras.stream()
                .sorted(Comparator.comparing(Obra::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Obra> consultar(@PathVariable Long id) {
        return obraRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Obra no encontrada"));
    }

    @PostMapping
    public ResponseEntity<Obra> crear(@Valid @RequestBody CrearObraRequest request) {
        Obra obra = Obra.builder()
                .autor(request.autor())
                .titulo(request.titulo())
                .resumen(request.resumen())
                .palabrasClave(request.palabrasClave().stream().toList())
                .build();

        Obra guardada = obraRepository.save(obra);
        return ResponseEntity.created(URI.create("/biblioteca/obras/" + guardada.getId())).body(guardada);
    }

    @PutMapping(path = "/{id}/digital", consumes = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
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
