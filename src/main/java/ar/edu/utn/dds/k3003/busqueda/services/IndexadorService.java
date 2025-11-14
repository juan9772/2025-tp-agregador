package ar.edu.utn.dds.k3003.busqueda.services;

public interface IndexadorService {

    /**
     * Indexa o re-indexa un hecho completo en la base de datos de búsqueda.
     * Este método debe obtener toda la información del hecho (incluyendo PDIs y otros datos)
     * y crear o actualizar el documento de HechoBusqueda en MongoDB.
     *
     * @param hechoId El ID del hecho a indexar.
     */
    void indexar(String hechoId);

    /**
     * Marca un hecho como borrado en la base de datos de búsqueda.
     * Esto se llamará cuando una solicitud de borrado sea aceptada.
     *
     * @param hechoId El ID del hecho a marcar como borrado.
     */
    void marcarComoBorrado(String hechoId);
}
