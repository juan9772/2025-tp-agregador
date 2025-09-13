package ar.edu.utn.dds.k3003.app;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import ar.edu.utn.dds.k3003.clients.FuenteProxy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.utn.dds.k3003.facades.FachadaAgregador;
import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.dtos.ConsensosEnum;
import ar.edu.utn.dds.k3003.facades.dtos.FuenteDTO;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import ar.edu.utn.dds.k3003.model.Agregador;
import ar.edu.utn.dds.k3003.model.Fuente;
import ar.edu.utn.dds.k3003.model.Hecho;
import ar.edu.utn.dds.k3003.repository.FuenteRepository;
import ar.edu.utn.dds.k3003.repository.InMemoryFuenteRepo;
import ar.edu.utn.dds.k3003.repository.JpaFuenteRepository;

@Service
public class Fachada implements FachadaAgregador {

  private Agregador agregador = new Agregador();

  private final FuenteRepository fuenteRepository;

  protected Fachada() {
    this.fuenteRepository = new InMemoryFuenteRepo();
  }

  @Autowired
  public Fachada(JpaFuenteRepository fuenteRepository) {
    this.fuenteRepository = fuenteRepository;
  }

  @Override
  public FuenteDTO agregar(FuenteDTO fuenteDto) {
    String id = UUID.randomUUID().toString();
    Fuente fuente = new Fuente(id, fuenteDto.nombre(), fuenteDto.endpoint());
    fuenteRepository.save(fuente);
    return convertirAFuenteDTO(agregador.agregarFuente(fuente));
  }

  @Override
  public List<FuenteDTO> fuentes() {
    return fuenteRepository.findAll().stream().map(this::convertirAFuenteDTO).collect(Collectors.toList());
  }

  @Override
  public FuenteDTO buscarFuenteXId(String fuenteId) throws NoSuchElementException {
    return fuenteRepository.findById(fuenteId)
        .map(this::convertirAFuenteDTO)
        .orElseThrow(() -> new NoSuchElementException("Fuente no encontrada: " + fuenteId));
  }

  @Override
  public List<HechoDTO> hechos(String nombreColeccion) throws NoSuchElementException {
    agregador.setLista_fuentes(fuenteRepository.findAll());
      List<Fuente> fuentes = fuenteRepository.findAll();

      // Por cada fuente, crea un FuenteProxy y lo añade al agregador.
      for (Fuente fuente : fuentes) {
          ObjectMapper objectMapper = new ObjectMapper();
          FuenteProxy fachadaProxy = new FuenteProxy(objectMapper, fuente.getEndpoint());
          agregador.agregarFachadaAFuente(fuente.getId(), fachadaProxy);
      }

      List<Hecho> hechosModelo = agregador.obtenerHechosPorColeccion(nombreColeccion);

    if (hechosModelo == null || hechosModelo.isEmpty()) {
      throw new NoSuchElementException("Busqueda no encontrada de: " + nombreColeccion);
    }
    return hechosModelo.stream()
        .map(this::convertirADTO)
        .collect(Collectors.toList());
  }

  @Override
  public void addFachadaFuentes(String fuenteId, FachadaFuente fuente) {
    agregador.agregarFachadaAFuente(fuenteId, fuente);
  }

  @Override
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