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

@WebMvcTest(ObraController.class)
class ObraControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ObraRepository obraRepository;

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

    @Test
    void filtraPorAutor() throws Exception {
        when(obraRepository.findByAutorContainingIgnoreCase("Borges")).thenReturn(List.of(
                obra(3L, "Jorge Luis Borges", "Ficciones", "Resumen", List.of("cuento"), null, null)));

        mockMvc.perform(get("/biblioteca/obras").param("autor", "Borges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].autor", is("Jorge Luis Borges")));
    }

    @Test
    void consultaUnaObraPorId() throws Exception {
        when(obraRepository.findById(7L)).thenReturn(Optional.of(
                obra(7L, "Mary Shelley", "Frankenstein", "Resumen", List.of("gotico"), null, null)));

        mockMvc.perform(get("/biblioteca/obras/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(7)))
                .andExpect(jsonPath("$.titulo", is("Frankenstein")));
    }

    @Test
    void devuelve404SiLaObraNoExiste() throws Exception {
        when(obraRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/biblioteca/obras/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void creaObraValida() throws Exception {
        when(obraRepository.save(any(Obra.class))).thenAnswer(invocation -> {
            Obra obra = invocation.getArgument(0);
            obra.setId(15L);
            return obra;
        });

        CrearObraRequest request = new CrearObraRequest(
                "Mary Shelley",
                "Frankenstein",
                "Novela gotica sobre ciencia y responsabilidad.",
                List.of("gotico", "ciencia", "clasico"));

        mockMvc.perform(post("/biblioteca/obras")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/biblioteca/obras/15"))
                .andExpect(jsonPath("$.id", is(15)))
                .andExpect(jsonPath("$.fechaDeposito").doesNotExist())
                .andExpect(jsonPath("$.urlCopiaDigital").doesNotExist());
    }

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

    @Test
    void devuelve404AlSubirSiLaObraNoExiste() throws Exception {
        when(obraRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(put("/biblioteca/obras/55/digital")
                        .contentType(APPLICATION_PDF)
                        .content(new byte[] { 1 }))
                .andExpect(status().isNotFound());
    }

    @Test
    void devuelve404AlDescargarSiLaObraNoExiste() throws Exception {
        when(obraRepository.findById(66L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/biblioteca/obras/66/digital"))
                .andExpect(status().isNotFound());
    }

    @Test
    void devuelve404AlDescargarSiNoHayPdf() throws Exception {
        when(obraRepository.findById(5L)).thenReturn(Optional.of(
                obra(5L, "Autor", "Titulo", "Resumen", List.of("clave"), null, null)));

        mockMvc.perform(get("/biblioteca/obras/5/digital"))
                .andExpect(status().isNotFound());
    }

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
