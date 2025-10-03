package ar.edu.utn.dds.k3003.model;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.dtos.ConsensosEnum;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class Agregador {

    private List<Fuente> lista_fuentes = new ArrayList<>();
    private Map<String, FachadaFuente> fachadaFuentes = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(Agregador.class);
    private Map<String, ConsensosEnum> tipoConsensoXColeccion = new HashMap<>();
    private FachadaSolicitudes fachadaSolicitudes;

    public Fuente agregarFuente(Fuente newFuente) {
        lista_fuentes.add(newFuente);
        return newFuente;
    }

    public void configurarConsenso(ConsensosEnum consenso, String nombreColeccion) {
        tipoConsensoXColeccion.put(nombreColeccion, consenso);
    }

    private List<Hecho> obtenerHechosDeTodasLasFuentes(String nombreColeccion) {
        List<Hecho> hechos = new ArrayList<>();

        for (Fuente fuente : lista_fuentes) {
            FachadaFuente fachada = fachadaFuentes.get(fuente.getId());

            logger.debug("Obteniendo hechos desde fuente id='{}' nombre='{}' con fachada={}", fuente.getId(), fuente.getNombre(), fachada != null);

            if (fachada != null) {
                try {
                    List<HechoDTO> hechosDTO = fachada.buscarHechosXColeccion(nombreColeccion);
                    logger.debug("Fuente id='{}' retornó {} hechosDTO", fuente.getId(), hechosDTO == null ? 0 : hechosDTO.size());
                    if (hechosDTO == null) {
                        logger.warn("Fuente id='{}' devolvió null para coleccion='{}' -> saltando", fuente.getId(), nombreColeccion);
                        continue;
                    }
                    hechos.addAll(
                            hechosDTO.stream()
                                    .map(dto -> {
                                        Hecho hecho = new Hecho(dto.titulo(), dto.id(), dto.nombreColeccion());
                                        hecho.setOrigen(fuente.getId());
                                        return hecho;
                                    }).toList());
                } catch (NoSuchElementException e) {
                    logger.warn("Fuente id='{}' no tiene hechos para coleccion='{}' : {}", fuente.getId(), nombreColeccion, e.getMessage());
                    continue;
                }
            }
        }

        return hechos;
    }

    public void setFachadaSolicitudes(FachadaSolicitudes fachadaSolicitudes) {
        this.fachadaSolicitudes = fachadaSolicitudes;
    }

    public List<Hecho> obtenerHechosPorColeccion(String nombreColeccion) {

        if (!tipoConsensoXColeccion.containsKey(nombreColeccion)) {
            logger.warn("No hay estrategia de consenso configurada para la colección '{}', devolviendo lista vacía.", nombreColeccion);
            return Collections.emptyList();
        }

        ConsensosEnum estrategia = tipoConsensoXColeccion.get(nombreColeccion);
        logger.info("Inicio de consenso para colección '{}' con estrategia '{}'", nombreColeccion, estrategia);

        List<Hecho> hechos = obtenerHechosDeTodasLasFuentes(nombreColeccion);
        logger.info("Se obtuvieron {} hechos en total de todas las fuentes para la colección '{}'", hechos.size(), nombreColeccion);

        Map<String, Hecho> hechosUnicos = hechos.stream()
                .collect(Collectors.toMap(
                        Hecho::getTitulo,
                        Function.identity(),
                        (existente, nuevo) -> existente));
        logger.info("Se encontraron {} hechos únicos (por título) para la colección '{}'", hechosUnicos.size(), nombreColeccion);

        switch (estrategia) {
            case TODOS:
                logger.info("Consenso TODOS: Devolviendo {} hechos únicos.", hechosUnicos.size());
                return new ArrayList<>(hechosUnicos.values());

            case AL_MENOS_2:
                logger.info("Consenso AL_MENOS_2: Analizando {} hechos únicos para la colección '{}'", hechosUnicos.size(), nombreColeccion);
                if (lista_fuentes.size() <= 1) {
                    logger.warn("Consenso AL_MENOS_2: Solo hay {} fuentes. No se puede aplicar el consenso de 'al menos 2'. Devolviendo todos los hechos únicos.", lista_fuentes.size());
                    return new ArrayList<>(hechosUnicos.values());
                } else {
                    Map<String, Set<String>> origenesPorTitulo = hechos.stream()
                            .collect(Collectors.groupingBy(Hecho::getTitulo,
                                    Collectors.mapping(Hecho::getOrigen, Collectors.toSet())));

                    Set<String> titulosRepetidos = origenesPorTitulo.entrySet().stream()
                            .filter(entry -> entry.getValue().size() >= 2)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toSet());
                    
                    logger.info("Consenso AL_MENOS_2: Se encontraron {} títulos de hechos presentes en al menos 2 fuentes.", titulosRepetidos.size());

                    List<Hecho> resultado = hechosUnicos.values().stream()
                        .filter(h -> titulosRepetidos.contains(h.getTitulo()))
                        .collect(Collectors.toList());

                    logger.info("Consenso AL_MENOS_2: Devolviendo {} hechos finales.", resultado.size());
                    return resultado;
                }

            case ESTRICTO:
                logger.info("Consenso ESTRICTO: Filtrando {} hechos únicos que no tengan solicitudes asociadas.", hechosUnicos.size());
                if (fachadaSolicitudes == null) {
                    logger.error("Consenso ESTRICTO: FachadaSolicitudes no configurada. No se puede continuar.");
                    throw new IllegalStateException("FachadaSolicitudes no configurada");
                }
                
                List<Hecho> hechosSinSolicitud = hechosUnicos.values().stream()
                        .filter(h -> {
                            boolean tieneSolicitud = !fachadaSolicitudes.buscarSolicitudXHecho(h.getId()).isEmpty();
                            if(tieneSolicitud) {
                                logger.debug("Consenso ESTRICTO: Descartando hecho '{}' (id: {}) porque tiene solicitudes asociadas.", h.getTitulo(), h.getId());
                            }
                            return !tieneSolicitud;
                        })
                        .collect(Collectors.toList());

                logger.info("Consenso ESTRICTO: Devolviendo {} hechos que no tienen solicitudes.", hechosSinSolicitud.size());
                return hechosSinSolicitud;

            default:
                logger.error("Estrategia de consenso no soportada: {}", estrategia);
                throw new IllegalArgumentException("Estrategia no soportada: " + estrategia);
        }
    }

    public void agregarFachadaAFuente(String fuenteId, FachadaFuente fuente) {
        Fuente existe_Fuente = lista_fuentes.stream()
                .filter(f -> f.getId().equals(fuenteId))
                .findAny()
                .orElse(null);

        if (existe_Fuente == null) {
            throw new NoSuchElementException("No se encontro la fuente");
        }
        logger.info("Agregando fachada para fuenteId='{}' (fuente encontrada nombre='{}')", fuenteId, existe_Fuente.getNombre());
        this.fachadaFuentes.put(fuenteId, fuente);
        existe_Fuente.setFachadaFuente(fuente);

    }
}
