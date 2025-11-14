package ar.edu.utn.dds.k3003.busqueda.config;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
    basePackages = "ar.edu.utn.dds.k3003.busqueda.repository",
    mongoTemplateRef = "searchMongoTemplate" // ID único para este MongoTemplate
)
public class MongoSearchConfig {

    // Inyecta la variable de entorno que definimos en agregador.env
    @Value("${SEARCH_DB_MONGO_URI}")
    private String mongoUri;

    // Crea el cliente de MongoDB a partir de la URI
    @Bean
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(mongoUri);
        return MongoClients.create(connectionString);
    }

    // Crea un MongoTemplate específico para la búsqueda, usando el cliente anterior.
    // Spring Data usará este Template para hablar con la base de datos de búsqueda.
    @Bean(name = "searchMongoTemplate")
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongoClient(), "Busquedas");
    }
}
