package ar.edu.utn.dds.k3003.busqueda.repository;

import ar.edu.utn.dds.k3003.busqueda.document.HechoBusqueda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HechoBusquedaRepository extends MongoRepository<HechoBusqueda, String> {

    /**
     * Busca hechos utilizando el índice de texto y filtra por tags (criterio AND).
     * Solo devuelve documentos que no han sido borrados.
     *
     * @param text El texto a buscar en los campos indexados.
     * @param tags La lista de tags que deben estar presentes en el documento.
     * @param pageable Objeto de paginación para limitar y ordenar los resultados.
     * @return Una página de documentos de HechoBusqueda que coinciden con los criterios.
     */
    @Query("{ $and: [ { $text: { $search: ?0 } }, { 'tags': { $all: ?1 } }, { 'fueBorrado': false } ] }")
    Page<HechoBusqueda> searchByTextAndTags(String text, List<String> tags, Pageable pageable);

    /**
     * Busca hechos utilizando el índice de texto, sin filtrar por tags.
     * Solo devuelve documentos que no han sido borrados.
     *
     * @param text El texto a buscar en los campos indexados.
     * @param pageable Objeto de paginación para limitar y ordenar los resultados.
     * @return Una página de HechoBusqueda que coinciden con el texto.
     */
    @Query("{ $and: [ { $text: { $search: ?0 } }, { 'fueBorrado': false } ] }")
    Page<HechoBusqueda> searchByText(String text, Pageable pageable);

    /**
     * Busca un documento por su nombre normalizado para evitar duplicados.
     *
     * @param nombreNormalizado El nombre del hecho en minúsculas y sin acentos.
     * @return Un Optional que contiene el documento si se encuentra.
     */
    Optional<HechoBusqueda> findByNombreHechoNormalizado(String nombreNormalizado);

}
