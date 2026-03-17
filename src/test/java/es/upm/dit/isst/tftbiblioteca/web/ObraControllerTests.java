package es.upm.dit.isst.tftbiblioteca.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.upm.dit.isst.tftbiblioteca.domain.Obra;
import es.upm.dit.isst.tftbiblioteca.domain.ObraRepository;

/**
 * Pruebas unitarias del controlador manual <code>/biblioteca/obras</code> usando
 * {@link WebMvcTest} para validar el contrato REST sin arrancar otros beans.
 */
@WebMvcTest(ObraController.class) // levanta únicamente la capa MVC y mockea el resto
class ObraControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean // reemplaza el bean real por un mock para aislar la capa web
    private ObraRepository obraRepository;

    /** Comprueba el listado completo cuando no se aplica filtro de autor. */
    @Test
    void listaTodasLasObras() throws Exception {
        when(obraRepository.findAll()).thenReturn(List.of(
                obra(1L, "Autor A", "Titulo A", "Resumen A", List.of("a"), null, null),
                obra(2L, "Autor B", "Titulo B", "Resumen B", List.of("b"), null, null)));

        mockMvc.perform(get("/biblioteca/obras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    /** Verifica que el parámetro <code>autor</code> filtra en el repositorio. */
    @Test
    void filtraPorAutor() throws Exception {
        when(obraRepository.findByAutorContainingIgnoreCase("Borges")).thenReturn(List.of(
                obra(3L, "Jorge Luis Borges", "Ficciones", "Resumen", List.of("cuento"), null, null)));

        mockMvc.perform(get("/biblioteca/obras").param("autor", "Borges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].autor", is("Jorge Luis Borges")));
    }

    /** Responde con 200 y el JSON esperado al consultar una obra existente. */
    @Test
    void consultaUnaObraPorId() throws Exception {
        when(obraRepository.findById(7L)).thenReturn(Optional.of(
                obra(7L, "Mary Shelley", "Frankenstein", "Resumen", List.of("gotico"), null, null)));

        mockMvc.perform(get("/biblioteca/obras/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(7)))
                .andExpect(jsonPath("$.titulo", is("Frankenstein")));
    }

    /** Responde 404 cuando el id solicitado no está en la base de datos. */
    @Test
    void devuelve404SiLaObraNoExiste() throws Exception {
        when(obraRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/biblioteca/obras/99"))
                .andExpect(status().isNotFound());
    }

    /**
     * Crea una obra válida y comprueba que se asigna id, cabecera Location y se
     * ocultan los campos calculados en la respuesta.
     */
    @Test
    void creaObraValida() throws Exception {
        when(obraRepository.save(any(Obra.class))).thenAnswer(invocation -> {
            Obra obra = invocation.getArgument(0);
            obra.setId(15L);
            return obra;
        });

        String request = """
                {
                  "autor": "Mary Shelley",
                  "titulo": "Frankenstein",
                  "resumen": "Novela gotica sobre ciencia y responsabilidad.",
                  "palabrasClave": ["gotico", "ciencia", "clasico"]
                }
                """;

        mockMvc.perform(post("/biblioteca/obras")
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/biblioteca/obras/15"))
                .andExpect(jsonPath("$.id", is(15)))
                .andExpect(jsonPath("$.fechaDeposito").doesNotExist())
                .andExpect(jsonPath("$.urlCopiaDigital").doesNotExist());
    }

    /** Rechaza el alta con datos que violan Bean Validation devolviendo 400. */
    @Test
    void rechazaAltaConCamposInvalidos() throws Exception {
        String requestInvalido = """
                {
                  "autor": "",
                  "titulo": "",
                  "resumen": "",
                  "palabrasClave": []
                }
                """;

        mockMvc.perform(post("/biblioteca/obras")
                        .contentType(APPLICATION_JSON)
                        .content(requestInvalido))
                .andExpect(status().isBadRequest());
    }

    /** Permite subir un PDF nuevo para una obra existente y devuelve 200. */
    @Test
    void subePdfValido() throws Exception {
        Obra obra = obra(4L, "Autor", "Titulo", "Resumen", List.of("clave"), null, null);
        when(obraRepository.findById(4L)).thenReturn(Optional.of(obra));
        when(obraRepository.save(any(Obra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/biblioteca/obras/4/digital")
                        .contentType(APPLICATION_PDF)
                        .content(new byte[] { 1, 2, 3 }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaDeposito", is(LocalDate.now().toString())))
                .andExpect(jsonPath("$.urlCopiaDigital", is("/biblioteca/obras/4/digital")));
    }

    /**
     * Al reemplazar un PDF existente, la fecha de depósito se actualiza y el
     * binario guardado coincide con el enviado.
     */
    @Test
    void reemplazaPdfYActualizaFechaDeposito() throws Exception {
        Obra obra = obra(8L, "Autor", "Titulo", "Resumen", List.of("clave"), new byte[] { 9 }, LocalDate.of(2025, 1, 1));
        when(obraRepository.findById(8L)).thenReturn(Optional.of(obra));
        when(obraRepository.save(any(Obra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/biblioteca/obras/8/digital")
                        .contentType(APPLICATION_PDF)
                        .content(new byte[] { 4, 5, 6, 7 }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaDeposito", is(LocalDate.now().toString())));

        ArgumentCaptor<Obra> captor = ArgumentCaptor.forClass(Obra.class);
        verify(obraRepository).save(captor.capture());
        Obra guardada = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(guardada.getCopiaDigitalPdf()).containsExactly(4, 5, 6, 7);
        org.assertj.core.api.Assertions.assertThat(guardada.getFechaDeposito()).isEqualTo(LocalDate.now());
    }

    /** Devuelve 404 al intentar subir un PDF para una obra inexistente. */
    @Test
    void devuelve404AlSubirSiLaObraNoExiste() throws Exception {
        when(obraRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(put("/biblioteca/obras/55/digital")
                        .contentType(APPLICATION_PDF)
                        .content(new byte[] { 1 }))
                .andExpect(status().isNotFound());
    }

    /** Devuelve 404 al descargar si la obra no existe. */
    @Test
    void devuelve404AlDescargarSiLaObraNoExiste() throws Exception {
        when(obraRepository.findById(66L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/biblioteca/obras/66/digital"))
                .andExpect(status().isNotFound());
    }

    /** Devuelve 404 al descargar cuando la obra existe pero no tiene PDF. */
    @Test
    void devuelve404AlDescargarSiNoHayPdf() throws Exception {
        when(obraRepository.findById(5L)).thenReturn(Optional.of(
                obra(5L, "Autor", "Titulo", "Resumen", List.of("clave"), null, null)));

        mockMvc.perform(get("/biblioteca/obras/5/digital"))
                .andExpect(status().isNotFound());
    }

    /** Descarga correcta: cabecera de attachment y contenido binario intacto. */
    @Test
    void descargaPdfConCabecerasCorrectas() throws Exception {
        byte[] pdf = new byte[] { 10, 20, 30 };
        when(obraRepository.findById(3L)).thenReturn(Optional.of(
                obra(3L, "Autor", "Titulo", "Resumen", List.of("clave"), pdf, LocalDate.of(2026, 3, 16))));

        mockMvc.perform(get("/biblioteca/obras/3/digital"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"obra-3.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(pdf));
    }

    /**
     * Helper para construir objetos de dominio sin repetir inicialización en
     * cada test del controlador.
     */
    private static Obra obra(Long id, String autor, String titulo, String resumen, List<String> palabrasClave,
            byte[] pdf, LocalDate fechaDeposito) {
        Obra obra = Obra.builder()
                .id(id)
                .autor(autor)
                .titulo(titulo)
                .resumen(resumen)
                .palabrasClave(palabrasClave)
                .fechaDeposito(fechaDeposito)
                .build();
        if (pdf != null) {
            obra.reemplazarCopiaDigital(pdf, fechaDeposito != null ? fechaDeposito : LocalDate.now());
        }
        return obra;
    }
}
