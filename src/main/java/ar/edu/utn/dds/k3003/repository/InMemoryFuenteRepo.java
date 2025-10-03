package ar.edu.utn.dds.k3003.repository;

import ar.edu.utn.dds.k3003.model.Fuente;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("test")
public class InMemoryFuenteRepo implements FuenteRepository {

    private List<Fuente> Fuentes;

    public InMemoryFuenteRepo() {
        this.Fuentes = new ArrayList<>();
    }

    @Override
    public Optional<Fuente> findById(String id) {
        return this.Fuentes.stream().filter(x -> x.getId().equals(id)).findFirst();
    }

    @Override
    public Fuente save(Fuente f) {
        this.Fuentes.add(f);
        return f;
    }

    @Override
    public List<Fuente> findAll() {
        return new ArrayList<>(Fuentes);
    }

    @Override
    public void deleteAll() {
        this.Fuentes.clear();
    }
}
