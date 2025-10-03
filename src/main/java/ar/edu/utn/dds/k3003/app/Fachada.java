package ar.edu.utn.dds.k3003.app;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import ar.edu.utn.dds.k3003.client.FuenteProxy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.dtos.ConsensosEnum;
import ar.edu.utn.dds.k3003.facades.dtos.FuenteDTO;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import ar.edu.utn.dds.k3003.model.Agregador;
import ar.edu.utn.dds.k3003.model.Fuente;
import ar.edu.utn.dds.k3003.model.Hecho;
import ar.edu.utn.dds.k3003.repository.FuenteRepository;
import ar.edu.utn.dds.k3003.repository.InMemoryFuenteRepo;
import ar.edu.utn.dds.k3003.repository.JpaFuenteRepository;

@Service
public class Fachada  {

  private Agregador agregador = new Agregador();
  private static final Logger logger = LoggerFactory.getLogger(Fachada.class);

  private final FuenteRepository fuenteRepository;

  protected Fachada() {
    this.fuenteRepository = new InMemoryFuenteRepo();
  }

  @Autowired
  public Fachada(JpaFuenteRepository fuenteRepository) {
    this.fuenteRepository = fuenteRepository;
  }


  public FuenteDTO agregar(FuenteDTO fuenteDto) {
    String id = UUID.randomUUID().toString();
    Fuente fuente = new Fuente(id, fuenteDto.nombre(), fuenteDto.endpoint());
    fuenteRepository.save(fuente);
    return convertirAFuenteDTO(agregador.agregarFuente(fuente));
  }


  public List<FuenteDTO> fuentes() {
    return fuenteRepository.findAll().stream().map(this::convertirAFuenteDTO).collect(Collectors.toList());
  }


  public FuenteDTO buscarFuenteXId(String fuenteId) throws NoSuchElementException {
    return fuenteRepository.findById(fuenteId)
        .map(this::convertirAFuenteDTO)
        .orElseThrow(() -> new NoSuchElementException("Fuente no encontrada: " + fuenteId));
  }


  public List<HechoDTO> hechos(String nombreColeccion) throws NoSuchElementException {
    logger.info("Inicio: listar hechos para coleccion='{}'", nombreColeccion);
    agregador.setLista_fuentes(fuenteRepository.findAll());
      List<Fuente> fuentes = fuenteRepository.findAll();

      // Por cada fuente, crea un FuenteProxy y lo añade al agregador.
      for (Fuente fuente : fuentes) {
          logger.info("Preparando Fachada para fuente id='{}' nombre='{}' endpoint='{}'", fuente.getId(), fuente.getNombre(), fuente.getEndpoint());
          ObjectMapper objectMapper = new ObjectMapper();
          FuenteProxy fachadaProxy = new FuenteProxy(objectMapper, fuente.getEndpoint());
          agregador.agregarFachadaAFuente(fuente.getId(), fachadaProxy);
      }

      List<Hecho> hechosModelo = agregador.obtenerHechosPorColeccion(nombreColeccion);

    if (hechosModelo == null || hechosModelo.isEmpty()) {
      logger.warn("No se encontraron hechos para coleccion='{}' (modelo retornó null/empty)", nombreColeccion);
      throw new NoSuchElementException("Busqueda no encontrada de: " + nombreColeccion);
    }
    List<HechoDTO> dto = hechosModelo.stream()
        .map(this::convertirADTO)
        .collect(Collectors.toList());
    logger.info("Fin: listar hechos para coleccion='{}' -> {} hechos DTO preparados", nombreColeccion, dto.size());
    return dto;
  }


  public void addFachadaFuentes(String fuenteId, FachadaFuente fuente) {
    agregador.agregarFachadaAFuente(fuenteId, fuente);
  }


  public void setConsensoStrategy(ConsensosEnum tipoConsenso, String nombreColeccion)
      throws InvalidParameterException {
    agregador.configurarConsenso(tipoConsenso, nombreColeccion);
  }

  private HechoDTO convertirADTO(Hecho hecho) {
    return new HechoDTO(hecho.getId(), hecho.getColeccionNombre(), hecho.getTitulo());
  }

  private FuenteDTO convertirAFuenteDTO(Fuente fuente) {
    return new FuenteDTO(fuente.getId(), fuente.getNombre(), fuente.getEndpoint());
  }

}