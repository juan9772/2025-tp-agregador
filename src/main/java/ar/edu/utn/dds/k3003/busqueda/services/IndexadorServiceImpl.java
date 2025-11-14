package ar.edu.utn.dds.k3003.busqueda.services;

import ar.edu.utn.dds.k3003.busqueda.document.HechoBusqueda;
import ar.edu.utn.dds.k3003.busqueda.repository.HechoBusquedaRepository;
import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IndexadorServiceImpl implements IndexadorService {

    private final ApiClientService apiClientService;
    private final HechoBusquedaRepository hechoBusquedaRepository;

    public IndexadorServiceImpl(ApiClientService apiClientService, HechoBusquedaRepository hechoBusquedaRepository) {
        this.apiClientService = apiClientService;
        this.hechoBusquedaRepository = hechoBusquedaRepository;
    }

    @Override
    public void indexar(String hechoId) {
        // 1. Obtener datos del hecho y sus PDIs
        Map<String, Object> hecho = apiClientService.obtenerHecho(hechoId);
        List<Map<String, Object>> pdis = apiClientService.obtenerPdisDeHecho(hechoId);
        // TODO: Obtener tags y resultados de herramientas externas cuando las APIs estén disponibles.

        String nombreHecho = (String) hecho.getOrDefault("titulo", "");
        String descripcionHecho = (String) hecho.getOrDefault("descripcion", "");

        // 2. Normalizar nombre para evitar duplicados
        String nombreNormalizado = normalizar(nombreHecho);

        // 3. Concatenar todo el texto relevante para la búsqueda
        StringBuilder textoCompleto = new StringBuilder();
        textoCompleto.append(nombreHecho).append(" ").append(descripcionHecho);
        for (Map<String, Object> pdi : pdis) {
            textoCompleto.append(" ").append(pdi.getOrDefault("descripcion", ""));
            textoCompleto.append(" ").append(pdi.getOrDefault("contenido", ""));
        }

        // 4. Buscar si el documento ya existe o crear uno nuevo
        HechoBusqueda doc = hechoBusquedaRepository.findByNombreHechoNormalizado(nombreNormalizado)
                .orElse(new HechoBusqueda());

        // 5. Poblar el documento de búsqueda
        doc.setId(hechoId);
        doc.setNombreHechoNormalizado(nombreNormalizado);
        doc.setDisplayNombre(nombreHecho);
        doc.setTextoBusqueda(textoCompleto.toString());
        doc.setFueBorrado(false);
        doc.setUltimoUpdate(Instant.now());

        // Añadir colección al documento (si no existe ya)
        String nombreColeccion = (String) hecho.getOrDefault("coleccionNombre", "");
        if (doc.getColecciones() == null) {
            doc.setColecciones(new java.util.ArrayList<>());
        }
        if (!nombreColeccion.isEmpty() && !doc.getColecciones().contains(nombreColeccion)) {
            doc.getColecciones().add(nombreColeccion);
        }

        // 6. Guardar en MongoDB
        hechoBusquedaRepository.save(doc);
    }

    @Override
    public void marcarComoBorrado(String hechoId) {
        Optional<HechoBusqueda> docOpt = hechoBusquedaRepository.findById(hechoId);
        if (docOpt.isPresent()) {
            HechoBusqueda doc = docOpt.get();
            doc.setFueBorrado(true);
            doc.setUltimoUpdate(Instant.now());
            hechoBusquedaRepository.save(doc);
        }
    }

    private String normalizar(String input) {
        if (input == null) return "";
        String texto = Normalizer.normalize(input, Normalizer.Form.NFD);
        return texto.replaceAll("[^\\p{ASCII}]", "").toLowerCase();
    }
}
