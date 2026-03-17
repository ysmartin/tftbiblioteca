package es.upm.dit.isst.tftbiblioteca.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import es.upm.dit.isst.tftbiblioteca.domain.Obra;
import es.upm.dit.isst.tftbiblioteca.domain.ObraRepository;

/**
 * Configuración que añade datos de demostración al iniciar la aplicación. En un
 * proyecto docente permite probar los endpoints REST sin pasos manuales de
 * alta previa.
 */
@Configuration // registra beans adicionales de arranque
public class BibliotecaDataLoader {

    /**
     * Bean que se ejecuta al arrancar la aplicación y pobla la base de datos si
     * está vacía. Actúa como sustituto de un script de migraciones para escenarios
     * demo.
     */
    @Bean // registra un CommandLineRunner que se ejecuta tras arrancar el contexto
    CommandLineRunner cargarObrasDemo(ObraRepository obraRepository) {
        return args -> {
            if (obraRepository.count() > 0) {
                return;
            }

            byte[] pdfDemo = cargarPdfDemo();

            Obra obraConPdf = Obra.builder()
                    .autor("Jorge Luis Borges")
                    .titulo("Ficciones")
                    .resumen("Coleccion de relatos breves con laberintos, bibliotecas y espejos.")
                    .palabrasClave(List.of("relatos", "clasico", "biblioteca"))
                    .build();
            obraConPdf.reemplazarCopiaDigital(pdfDemo, LocalDate.of(2026, 1, 10));

            Obra obraSinPdf = Obra.builder()
                    .autor("Jorge Luis Borges")
                    .titulo("El Aleph")
                    .resumen("Relatos sobre memoria, infinito y percepcion.")
                    .palabrasClave(List.of("cuento", "infinito"))
                    .build();

            Obra obraOrwell = Obra.builder()
                    .autor("George Orwell")
                    .titulo("1984")
                    .resumen("Novela distopica sobre vigilancia, lenguaje y poder.")
                    .palabrasClave(List.of("distopia", "politica", "novela"))
                    .build();

            Obra obraAusten = Obra.builder()
                    .autor("Jane Austen")
                    .titulo("Orgullo y prejuicio")
                    .resumen("Novela sobre clase social, prejuicios y relaciones familiares.")
                    .palabrasClave(List.of("novela", "sociedad", "clasico"))
                    .build();

            obraRepository.saveAll(List.of(obraConPdf, obraSinPdf, obraOrwell, obraAusten));
        };
    }

    /**
     * Lee el PDF incluido en <code>src/main/resources/documents/</code> para
     * reutilizarlo como binario de ejemplo en las obras precargadas.
     */
    private byte[] cargarPdfDemo() {
        try {
            return new ClassPathResource("documents/obra-demo.pdf").getInputStream().readAllBytes();
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo cargar el PDF de ejemplo", ex);
        }
    }
}
