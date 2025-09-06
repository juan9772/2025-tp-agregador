package ar.edu.utn.dds.k3003.repository;

import java.util.List;
import java.util.Optional;

import ar.edu.utn.dds.k3003.model.Hecho;

public interface HechoRepository {

    Optional<Hecho> findById(String id);

    Hecho save(Hecho hecho);

    List<Hecho> findAll();

}
