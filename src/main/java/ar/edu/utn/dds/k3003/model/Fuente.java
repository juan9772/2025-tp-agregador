package ar.edu.utn.dds.k3003.model;

import java.util.ArrayList;
import java.util.List;

import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "Fuente")
@NoArgsConstructor
public class Fuente {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "endpoint", nullable = false)
    private String endpoint;

    @Transient
    private FachadaFuente fachadaFuente;

    @OneToMany(mappedBy = "fuente", cascade = CascadeType.ALL)
    private List<Hecho> lista_hechos = new ArrayList<>();

    public List<Hecho> obtenerHechos(String coleccionId) {
        return lista_hechos;
    }

    public Fuente(String id, String nombre, String endpoint) {
        this.id = id;
        this.nombre = nombre;
        this.endpoint = endpoint;
    }
}
