package ar.edu.utn.dds.k3003.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para el módulo agregador.
 * Consume eventos de los otros módulos para mantener el índice de búsqueda sincronizado.
 */
@Configuration
public class RabbitConfig {

    // Exchange principal para todos los eventos
    public static final String TOPIC_EXCHANGE_NAME = "hechos-topic-exchange";

    // Cola exclusiva del agregador para indexación
    public static final String INDEXACION_QUEUE = "agregador-indexacion-queue";

    // Routing keys para diferentes tipos de eventos
    public static final String HECHO_CREADO_KEY = "hecho.creado";
    public static final String HECHO_ACTUALIZADO_KEY = "hecho.actualizado";
    public static final String PDI_PROCESADO_KEY = "pdi.procesado";
    public static final String HECHO_BORRADO_KEY = "hecho.borrado";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue indexacionQueue() {
        return new Queue(INDEXACION_QUEUE, true);
    }

    /**
     * Binding: la cola del agregador escucha TODOS los eventos relevantes.
     * Usamos patrón "hecho.*" y "pdi.*" para capturar todos los eventos.
     */
    @Bean
    public Binding bindingHechos(Queue indexacionQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(indexacionQueue).to(topicExchange).with("hecho.*");
    }

    @Bean
    public Binding bindingPdis(Queue indexacionQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(indexacionQueue).to(topicExchange).with("pdi.*");
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public org.springframework.boot.ApplicationRunner runner(RabbitAdmin rabbitAdmin) {
        return args -> rabbitAdmin.initialize();
    }
}
