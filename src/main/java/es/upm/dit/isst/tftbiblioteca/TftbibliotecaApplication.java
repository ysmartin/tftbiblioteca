package es.upm.dit.isst.tftbiblioteca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación Spring Boot. La anotación
 * {@link SpringBootApplication} activa el escaneo de componentes, la
 * configuración automática y el soporte de Spring Data REST que usamos para
 * exponer el repositorio en paralelo a la API manual.
 */
@SpringBootApplication // habilita autoconfiguración, escaneo de componentes y soporte Data REST
public class TftbibliotecaApplication {

	/**
	 * Arranca el contenedor embebido y deja disponibles los controladores REST y
	 * el catálogo de datos JPA.
	 */
	public static void main(String[] args) {
		SpringApplication.run(TftbibliotecaApplication.class, args);
	}

}
