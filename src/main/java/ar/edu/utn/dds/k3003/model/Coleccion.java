package ar.edu.utn.dds.k3003.model;

import java.time.LocalDateTime;
import lombok.Data;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Data
@Entity
public class Coleccion {

  public Coleccion(String nombre, String descripcion) {
    this.nombre = nombre;
    this.descripcion = descripcion;
  }

  public Coleccion(String nombre) {
    this.nombre = nombre;
  }

  @Id
  private String nombre;
  private String descripcion;
  private LocalDateTime fechaModificacion;

}