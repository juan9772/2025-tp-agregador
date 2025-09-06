package ar.edu.utn.dds.k3003.repository;

import java.util.List;
import java.util.Optional;

import ar.edu.utn.dds.k3003.model.Fuente;

public interface FuenteRepository {

    Optional<Fuente> findById(String id);

    Fuente save(Fuente fuente);

    List<Fuente> findAll();
}
