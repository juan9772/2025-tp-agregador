package ar.edu.utn.dds.k3003.client;

import com.fasterxml.jackson.annotation.JsonAlias;

// Mixin to help deserialize HechoDTO when remote fuente uses camelCase 'nombreColeccion'
public abstract class HechoDTOMixin {

    @JsonAlias({"nombreColeccion"})
    abstract String nombreColeccion();

}
