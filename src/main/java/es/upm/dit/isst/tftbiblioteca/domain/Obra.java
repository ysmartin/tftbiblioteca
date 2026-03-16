package es.upm.dit.isst.tftbiblioteca.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

@Entity
@Table(name = "obras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Obra {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    private String autor;

    @NotBlank
    @Size(max = 250)
    private String titulo;

    @NotBlank
    @Size(max = 4000)
    @Column(length = 4000)
    private String resumen;

    @ElementCollection
    @CollectionTable(name = "obra_palabras_clave", joinColumns = @JoinColumn(name = "obra_id"))
    @Column(name = "palabra_clave", nullable = false)
    @NotEmpty
    @Size(max = 10)
    @Default
    private List<@NotBlank @Size(max = 50) String> palabrasClave = new ArrayList<>();

    @Lob
    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private byte[] copiaDigitalPdf;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDate fechaDeposito;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getUrlCopiaDigital() {
        return tieneCopiaDigital() && id != null ? "/biblioteca/obras/" + id + "/digital" : null;
    }

    @JsonIgnore
    public byte[] getCopiaDigitalPdf() {
        return copiaDigitalPdf;
    }

    public boolean tieneCopiaDigital() {
        return copiaDigitalPdf != null && copiaDigitalPdf.length > 0;
    }

    public void reemplazarCopiaDigital(byte[] pdf, LocalDate nuevaFechaDeposito) {
        this.copiaDigitalPdf = Objects.requireNonNull(pdf, "El PDF es obligatorio");
        this.fechaDeposito = Objects.requireNonNull(nuevaFechaDeposito, "La fecha de deposito es obligatoria");
    }
}
