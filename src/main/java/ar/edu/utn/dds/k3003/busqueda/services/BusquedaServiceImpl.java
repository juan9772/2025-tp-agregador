package ar.edu.utn.dds.k3003.busqueda.services;

import ar.edu.utn.dds.k3003.busqueda.document.HechoBusqueda;
import ar.edu.utn.dds.k3003.busqueda.repository.HechoBusquedaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BusquedaServiceImpl implements BusquedaService {

    private final HechoBusquedaRepository hechoBusquedaRepository;

    public BusquedaServiceImpl(HechoBusquedaRepository hechoBusquedaRepository) {
        this.hechoBusquedaRepository = hechoBusquedaRepository;
    }

    @Override
    public Page<HechoBusqueda> buscar(String query, int page, int size) {
        // 1. Parsear la consulta para separar texto y tags
        List<String> parts = Arrays.asList(query.split("\\s+"));
        List<String> tags = parts.stream()
                .filter(p -> p.startsWith("tag:"))
                .map(p -> p.substring(4).toLowerCase()) // Normalizar a minúsculas
                .collect(Collectors.toList());

        String searchText = parts.stream()
                .filter(p -> !p.startsWith("tag:"))
                .collect(Collectors.joining(" ")).trim();

        // 2. Crear el objeto de paginación
        Pageable pageable = PageRequest.of(page, size);

        // 3. Ejecutar la consulta adecuada
        boolean hasText = !searchText.isEmpty();
        boolean hasTags = !tags.isEmpty();

        if (hasText && hasTags) {
            return hechoBusquedaRepository.searchByTextAndTags(searchText, tags, pageable);
        } else if (hasText) {
            return hechoBusquedaRepository.searchByText(searchText, pageable);
        } else if (hasTags) {
            return hechoBusquedaRepository.searchByTags(tags, pageable);
        } else {
            // Si no hay ni texto ni tags, devolver una página vacía o todos los hechos, según se prefiera.
            // Aquí devolvemos una página vacía para evitar una carga masiva no intencionada.
            return Page.empty(pageable);
        }
    }
}
