package ar.edu.utn.dds.k3003.model;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.dtos.ConsensosEnum;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import lombok.Data;

@Data
public class Agregador {

    private List<Fuente> lista_fuentes = new ArrayList<>();
    private Map<String, FachadaFuente> fachadaFuentes = new HashMap<>();
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

            if (fachada != null) {
                try {
                    List<HechoDTO> hechosDTO = fachada.buscarHechosXColeccion(nombreColeccion);
                    hechos.addAll(
                            hechosDTO.stream()
                                    .map(dto -> {
                                        Hecho hecho = new Hecho(dto.titulo(), dto.id(), dto.nombreColeccion());
                                        hecho.setOrigen(fuente.getId());
                                        return hecho;
                                    }).toList());
                } catch (NoSuchElementException e) {
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
            return Collections.emptyList();
        }

        ConsensosEnum estrategia = tipoConsensoXColeccion.get(nombreColeccion);
        List<Hecho> hechos = obtenerHechosDeTodasLasFuentes(nombreColeccion);
        Map<String, Hecho> hechosUnicos = hechos.stream()
                .collect(Collectors.toMap(
                        Hecho::getTitulo,
                        Function.identity(),
                        (existente, nuevo) -> existente));
        switch (estrategia) {
            case TODOS:
                return new ArrayList<>(hechosUnicos.values());
            case AL_MENOS_2:
                if (lista_fuentes.size() == 1) {
                    return new ArrayList<>(hechosUnicos.values());
                } else {
                    Set<String> titulos_Repetidos = hechos.stream()
                            .collect(Collectors.groupingBy(Hecho::getTitulo,
                                    Collectors.mapping(Hecho::getOrigen, Collectors.toSet())))
                            .entrySet().stream()
                            .filter(e -> e.getValue().size() >= 2)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toSet());
                    return hechos.stream().filter(h -> titulos_Repetidos.contains(h.getTitulo()))
                            .collect(Collectors.toMap(
                                    Hecho::getTitulo, Function.identity(),
                                    (h1, h2) -> h1))
                            .values().stream().collect(Collectors.toList());
                }
            case ESTRICTO:
                if (fachadaSolicitudes == null) {
                    throw new IllegalStateException("FachadaSolicitudes no configurada");
                }
                return hechos.stream()
                        .filter(h -> fachadaSolicitudes.buscarSolicitudXHecho(h.getId()).isEmpty())
                        .collect(Collectors.toList());
            default:
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
        this.fachadaFuentes.put(fuenteId, fuente);
        existe_Fuente.setFachadaFuente(fuente);

    }
}