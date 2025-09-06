package ar.edu.utn.dds.k3003.repository;

import ar.edu.utn.dds.k3003.model.Hecho;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("test")
public class InMemoryHechoRepo implements HechoRepository {

    private List<Hecho> hechos;

    public InMemoryHechoRepo() {
        this.hechos = new ArrayList<>();
    }

    @Override
    public Optional<Hecho> findById(String id) {
        return this.hechos.stream().filter(x -> x.getId().equals(id)).findFirst();
    }

    @Override
    public Hecho save(Hecho h) {
        this.hechos.add(h);
        h.setFecha_carga(LocalDate.now());
        return h;
    }

    @Override
    public List<Hecho> findAll() {
        return new ArrayList<>(hechos);
    }
}