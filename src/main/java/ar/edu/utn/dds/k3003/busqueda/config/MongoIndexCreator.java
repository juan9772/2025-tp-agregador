package ar.edu.utn.dds.k3003.busqueda.config;

import ar.edu.utn.dds.k3003.busqueda.document.HechoBusqueda;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MongoIndexCreator {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoIndexCreator(@Qualifier("searchMongoTemplate") MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void init() {
        log.info("Asegurando la creación de índices de texto para HechoBusqueda...");
        try {
            IndexOperations indexOps = mongoTemplate.indexOps(HechoBusqueda.class);

            // Definir el índice de texto en el campo "textoBusqueda"
            TextIndexDefinition textIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
                    .onField("textoBusqueda")
                    .build();

            indexOps.ensureIndex(textIndex);
            log.info("Índice de texto para HechoBusqueda asegurado correctamente.");

        } catch (Exception e) {
            log.error("Error al crear el índice de texto para HechoBusqueda", e);
        }
    }
}
