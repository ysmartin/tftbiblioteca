package es.upm.dit.isst.tftbiblioteca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import es.upm.dit.isst.tftbiblioteca.domain.ObraRepository;

@SpringBootTest
@AutoConfigureMockMvc
class TftbibliotecaApplicationTests {

	@Autowired
	private ObraRepository obraRepository;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void cargaElContextoYLosDatosDemo() {
		assertThat(obraRepository.count()).isGreaterThanOrEqualTo(4);
	}

	@Test
	void exponeSpringDataRestYSwaggerUi() throws Exception {
		mockMvc.perform(get("/api/obras"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/swagger-ui/index.html"))
				.andExpect(status().isOk());
	}

}
