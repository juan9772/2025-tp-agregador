package ar.edu.utn.dds.k3003.busqueda.services;

import ar.edu.utn.dds.k3003.busqueda.document.HechoBusqueda;
import ar.edu.utn.dds.k3003.busqueda.repository.HechoBusquedaRepository;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BusquedaServiceImpl implements BusquedaService {

    private final HechoBusquedaRepository hechoBusquedaRepository;
    private final MongoTemplate mongoTemplate;

    public BusquedaServiceImpl(HechoBusquedaRepository hechoBusquedaRepository, MongoTemplate mongoTemplate) {
        this.hechoBusquedaRepository = hechoBusquedaRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<HechoBusqueda> buscar(String queryText, int page, int size) {

        List<String> tags = new ArrayList<>();
        Pattern tagPattern = Pattern.compile("tag:\"([^\"]+)\"|tag:(\\S+)");
        Matcher matcher = tagPattern.matcher(queryText);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String tagValue = (matcher.group(1) != null) ? matcher.group(1) : matcher.group(2);
            tags.add(tagValue); // No es necesario el toLowerCase() ahora
            matcher.appendReplacement(sb, "");
        }
        matcher.appendTail(sb);
        String searchText = sb.toString().replaceAll("\\s+", " ").trim();

        Pageable pageable = PageRequest.of(page, size);

        boolean hasText = !searchText.isEmpty();
        boolean hasTags = !tags.isEmpty();

        if (!hasText && !hasTags) {
            return Page.empty(pageable);
        }

        // --- Búsqueda en PDIs --- //
        List<String> hechoIdsFromPdis = new ArrayList<>();
        List<Criteria> pdiCriterias = new ArrayList<>();
        if (hasText) {
            pdiCriterias.add(Criteria.where("textoBusqueda").regex(searchText, "i"));
        }
        if (hasTags) {
            tags.forEach(tag -> pdiCriterias.add(Criteria.where("etiquetasAuto").regex(tag, "i")));
        }
        if (!pdiCriterias.isEmpty()){
             Query pdiQuery = new Query(new Criteria().andOperator(pdiCriterias.toArray(new Criteria[0])));
             hechoIdsFromPdis = mongoTemplate.find(pdiQuery, Document.class, "pdis_busqueda")
                .stream()
                .map(pdi -> pdi.getString("hechoId"))
                .distinct()
                .toList();
        }

        // --- Búsqueda en Hechos --- //
        Query hechoQuery = new Query().with(pageable);

        // Criterios directos en Hecho (texto Y tags)
        List<Criteria> hechoDirectCriterias = new ArrayList<>();
        if (hasText) {
            hechoDirectCriterias.add(Criteria.where("textoBusqueda").regex(searchText, "i"));
        }
        if (hasTags) {
            tags.forEach(tag -> hechoDirectCriterias.add(Criteria.where("tags").regex(tag, "i")));
        }

        List<Criteria> orGlobalCriteria = new ArrayList<>();
        if (!hechoDirectCriterias.isEmpty()){
             orGlobalCriteria.add(new Criteria().andOperator(hechoDirectCriterias.toArray(new Criteria[0])));
        }
       
        if (!hechoIdsFromPdis.isEmpty()) {
            orGlobalCriteria.add(Criteria.where("id").in(hechoIdsFromPdis));
        }

        if(orGlobalCriteria.isEmpty()){
            return Page.empty(pageable);
        }

        hechoQuery.addCriteria(new Criteria().orOperator(orGlobalCriteria.toArray(new Criteria[0])));
        hechoQuery.addCriteria(Criteria.where("fueBorrado").is(false));

        long count = mongoTemplate.count(hechoQuery, HechoBusqueda.class);
        List<HechoBusqueda> resultados = mongoTemplate.find(hechoQuery, HechoBusqueda.class);

        return new PageImpl<>(resultados, pageable, count);
    }
}
