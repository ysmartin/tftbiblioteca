package es.upm.dit.isst.tftbiblioteca.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Pruebas de persistencia que validan el comportamiento del repositorio JPA y
 * su consulta derivada por autor.
 */
@DataJpaTest // configura un slice de JPA en memoria para tests rápidos
class ObraRepositoryTests {

    @Autowired
    private ObraRepository obraRepository;

    /** Comprueba que se persisten obras con y sin varias palabras clave. */
    @Test
    void persisteYRecuperaObrasConYsinPalabrasClave() {
        Obra primera = obraRepository.save(Obra.builder()
                .autor("Autor Uno")
                .titulo("Titulo Uno")
                .resumen("Resumen de prueba")
                .palabrasClave(List.of("clave1", "clave2"))
                .build());

        Obra segunda = obraRepository.save(Obra.builder()
                .autor("Autor Dos")
                .titulo("Titulo Dos")
                .resumen("Otro resumen")
                .palabrasClave(List.of("unica"))
                .build());

        List<Obra> obras = StreamSupport.stream(obraRepository.findAll().spliterator(), false).toList();

        assertThat(obras)
                .extracting(Obra::getId)
                .contains(primera.getId(), segunda.getId());
        assertThat(obras)
                .extracting(Obra::getTitulo, obra -> obra.getPalabrasClave().size())
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("Titulo Uno", 2),
                        org.assertj.core.groups.Tuple.tuple("Titulo Dos", 1));
    }

    /** Verifica la consulta derivada ignorando mayúsculas y minúsculas. */
    @Test
    void filtraPorAutorIgnorandoMayusculas() {
        obraRepository.save(Obra.builder()
                .autor("Gabriel Garcia Marquez")
                .titulo("Cien anos de soledad")
                .resumen("Macondo y varias generaciones.")
                .palabrasClave(List.of("realismo", "novela"))
                .build());
        obraRepository.save(Obra.builder()
                .autor("gABriel garcia marquez")
                .titulo("El coronel no tiene quien le escriba")
                .resumen("Espera, dignidad y precariedad.")
                .palabrasClave(List.of("novela corta"))
                .build());
        obraRepository.save(Obra.builder()
                .autor("Clarice Lispector")
                .titulo("La hora de la estrella")
                .resumen("Historia de Macabea.")
                .palabrasClave(List.of("brasil"))
                .build());

        List<Obra> obras = obraRepository.findByAutorContainingIgnoreCase("garcia");

        assertThat(obras).hasSize(2);
        assertThat(obras)
                .extracting(Obra::getTitulo)
                .containsExactlyInAnyOrder("Cien anos de soledad", "El coronel no tiene quien le escriba");
    }

    /** Devuelve lista vacía cuando no hay coincidencias para el autor buscado. */
    @Test
    void devuelveListaVaciaSiNoHayCoincidencias() {
        obraRepository.save(Obra.builder()
                .autor("Ursula K. Le Guin")
                .titulo("Los desposeidos")
                .resumen("Dos sociedades en tension.")
                .palabrasClave(List.of("ciencia ficcion"))
                .build());

        List<Obra> obras = obraRepository.findByAutorContainingIgnoreCase("borges");

        assertThat(obras).isEmpty();
    }
}
