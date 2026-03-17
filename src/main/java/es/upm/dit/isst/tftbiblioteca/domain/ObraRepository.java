package es.upm.dit.isst.tftbiblioteca.domain;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

/**
 * Repositorio Spring Data para la entidad {@link Obra}, usado desde el controlador.
 *
 * Además del controlador manual, Spring Data REST expone el repositorio como una segunda API REST. 
 * La anotación {@link RepositoryRestResource} permite particularizar la URI donde
 * se publica a <code>/api/obras</code>
 */
@RepositoryRestResource(collectionResourceRel = "obras", path = "obras") // habilita la superficie automática de Spring Data REST
public interface ObraRepository extends CrudRepository<Obra, Long> {

    /**
     * Consulta auxiliar para búsqueda de obras por autor, ignorando mayúsculas/minúsculas.
     * 
     * Reutilizada por Spring Data REST para exponer un endpoint de búsqueda adicional.
     * La anotación {@link RestResource}  particulariza la ruta (GET /api/obras/search/porAutor?autor=...)
     */
    @RestResource(path = "porAutor", rel = "porAutor") // expone la query derivada como endpoint en Spring Data REST
    List<Obra> findByAutorContainingIgnoreCase(String autor);
}
