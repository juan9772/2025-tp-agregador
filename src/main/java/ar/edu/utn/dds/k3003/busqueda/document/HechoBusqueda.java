package ar.edu.utn.dds.k3003.busqueda.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * Documento denormalizado para optimizar las búsquedas de hechos.
 * Cada documento representa un hecho único (por nombre) y agrupa
 * toda la información relevante para la búsqueda.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "hechos_busqueda")
@CompoundIndex(name = "texto_busqueda_index", def = "{'textoBusqueda': 'text'}")
public class HechoBusqueda {

    @Id
    private String id; // ID del Hecho original en la base de datos principal

    @Indexed(unique = true)
    private String nombreHechoNormalizado; // Para evitar duplicados

    private String displayNombre;

    private List<String> colecciones;

    @Indexed
    private List<String> tags;

    private String textoBusqueda;

    @Indexed
    private boolean fueBorrado = false;

    private Instant ultimoUpdate;
}
