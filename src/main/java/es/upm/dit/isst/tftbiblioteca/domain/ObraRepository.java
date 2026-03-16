package es.upm.dit.isst.tftbiblioteca.domain;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(collectionResourceRel = "obras", path = "obras")
public interface ObraRepository extends CrudRepository<Obra, Long> {

    @RestResource(path = "porAutor", rel = "porAutor")
    List<Obra> findByAutorContainingIgnoreCase(String autor);
}
