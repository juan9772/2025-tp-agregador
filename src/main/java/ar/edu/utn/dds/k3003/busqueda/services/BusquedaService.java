package ar.edu.utn.dds.k3003.busqueda.services;

import ar.edu.utn.dds.k3003.busqueda.document.HechoBusqueda;
import org.springframework.data.domain.Page;

public interface BusquedaService {

    /**
     * Realiza una búsqueda de hechos por palabras clave y tags opcionales.
     *
     * @param query La consulta del usuario. Ej: "incendio", "incendio tag:CABA tag:URGENTE"
     * @param page El número de página a devolver (empezando en 0).
     * @param size El tamaño de la página.
     * @return Una página de documentos HechoBusqueda que coinciden con la búsqueda.
     */
    Page<HechoBusqueda> buscar(String query, int page, int size);

}
