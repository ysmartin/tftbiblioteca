package es.upm.dit.isst.tftbiblioteca.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA que representa una obra en el catálogo. Se utiliza tanto por el
 * controlador REST manual como por la exposición automática de Spring Data
 * REST, de modo que el mismo modelo se serializa en JSON y se persiste en base
 * de datos H2.
 */
@Entity
@Table(name = "obras")
@JsonIgnoreProperties(ignoreUnknown = false) // rechaza campos desconocidos en el JSON para mantener contrato estricto
@Getter // Lombok genera getters de todos los campos para serialización y tests
@Setter // Lombok genera setters necesarios para JPA y para actualizar PDFs
@NoArgsConstructor // requerido por JPA para instanciar entidades vía reflexión
@AllArgsConstructor // facilita creación completa en pruebas
@Builder // habilita el patrón builder para crear obras de forma legible en controladores y tests
public class Obra {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // delega en JPA la generación de la clave primaria en H2
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // el cliente recibe el id pero no lo envía en el alta
    private Long id;

    @NotBlank
    @Size(max = 200)
    private String autor;

    @NotBlank
    @Size(max = 250)
    private String titulo;

    @NotBlank
    @Size(max = 4000)
    @Column(length = 4000) // ajusta la columna para textos largos sin crear un @Lob
    private String resumen;

    @ElementCollection // mapea la lista simple a una tabla secundaria gestionada por JPA
    @CollectionTable(name = "obra_palabras_clave", joinColumns = @JoinColumn(name = "obra_id")) // tabla secundaria sin entidad propia
    @Column(name = "palabra_clave", nullable = false) // fuerza que cada palabra clave se guarde como fila obligatoria
    @NotEmpty
    @Size(max = 10)
    @Default // indica a Lombok que inicialice esta lista al usar el builder
    private List<@NotBlank @Size(max = 50) String> palabrasClave = new ArrayList<>();

    @Lob // almacena el binario en la base de datos sin romper el esquema simple de la demo
    @JsonIgnore // evita exponer el binario en las respuestas JSON
    @Getter(AccessLevel.NONE) // Lombok no genera getter para controlar la visibilidad manualmente
    @Setter(AccessLevel.NONE) // idem para el setter
    private byte[] copiaDigitalPdf;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // fecha calculada por el servidor al subir un PDF
    private LocalDate fechaDeposito;

    /**
     * Devuelve la URL REST de descarga que se expone al cliente solo cuando ya
     * existe un PDF cargado y la obra tiene identificador asignado.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getUrlCopiaDigital() {
        return tieneCopiaDigital() && id != null ? "/biblioteca/obras/" + id + "/digital" : null;
    }

    /**
     * Permite al controlador obtener el binario para la respuesta HTTP sin
     * serializarlo en JSON.
     */
    @JsonIgnore
    public byte[] getCopiaDigitalPdf() {
        return copiaDigitalPdf;
    }

    /**
     * Indica si la obra ya tiene una copia digital almacenada, lo que condiciona
     * la respuesta de los endpoints de descarga y de alta de binarios.
     */
    public boolean tieneCopiaDigital() {
        return copiaDigitalPdf != null && copiaDigitalPdf.length > 0;
    }

    /**
     * Reemplaza el PDF almacenado y actualiza la fecha de depósito. Se usa desde
     * la capa web para mantener la lógica de dominio en un único lugar.
     *
     * @param pdf               nuevo contenido binario
     * @param nuevaFechaDeposito fecha de referencia que se mostrará al cliente
     */
    public void reemplazarCopiaDigital(byte[] pdf, LocalDate nuevaFechaDeposito) {
        this.copiaDigitalPdf = Objects.requireNonNull(pdf, "El PDF es obligatorio");
        this.fechaDeposito = Objects.requireNonNull(nuevaFechaDeposito, "La fecha de deposito es obligatoria");
    }
}
