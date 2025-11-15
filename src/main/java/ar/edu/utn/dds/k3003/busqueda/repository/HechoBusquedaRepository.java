package ar.edu.utn.dds.k3003.busqueda.repository;

import ar.edu.utn.dds.k3003.busqueda.document.HechoBusqueda;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HechoBusquedaRepository extends MongoRepository<HechoBusqueda, String> {

    /**
     * Busca un documento por su nombre normalizado para evitar duplicados.
     *
     * @param nombreNormalizado El nombre del hecho en minúsculas y sin acentos.
     * @return Un Optional que contiene el documento si se encuentra.
     */
    Optional<HechoBusqueda> findByNombreHechoNormalizado(String nombreNormalizado);

}
