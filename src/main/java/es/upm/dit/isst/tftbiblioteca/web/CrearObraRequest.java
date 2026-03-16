package es.upm.dit.isst.tftbiblioteca.web;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = false)
public record CrearObraRequest(
        @NotBlank @Size(max = 200) String autor,
        @NotBlank @Size(max = 250) String titulo,
        @NotBlank @Size(max = 4000) String resumen,
        @NotEmpty @Size(max = 10) List<@NotBlank @Size(max = 50) String> palabrasClave) {
}
