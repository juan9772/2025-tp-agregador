package ar.edu.utn.dds.k3003.workers;

import ar.edu.utn.dds.k3003.busqueda.services.IndexadorService;
import ar.edu.utn.dds.k3003.config.RabbitConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Worker que escucha eventos de otros módulos y mantiene el índice de búsqueda actualizado.
 * Implementa consistencia eventual: los cambios en otros módulos se propagan aquí mediante eventos.
 */
@Service
public class IndexadorWorker {

    private final IndexadorService indexadorService;
    private final ObjectMapper objectMapper;

    public IndexadorWorker(IndexadorService indexadorService) {
        this.indexadorService = indexadorService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Escucha la cola de indexación y procesa los eventos entrantes.
     * Formato esperado del mensaje JSON:
     * {
     *   "eventType": "HECHO_CREADO" | "HECHO_ACTUALIZADO" | "PDI_PROCESADO" | "HECHO_BORRADO",
     *   "hechoId": "abc123",
     *   "pdiId": 456 (opcional, solo para PDI_PROCESADO)
     * }
     */
    @RabbitListener(queues = RabbitConfig.INDEXACION_QUEUE)
    public void procesarEvento(String mensaje) {
        try {
            System.out.println("[IndexadorWorker] Evento recibido: " + mensaje);

            JsonNode event = objectMapper.readTree(mensaje);
            String eventType = event.get("eventType").asText();
            String hechoId = event.get("hechoId").asText();

            switch (eventType) {
                case "HECHO_CREADO":
                case "HECHO_ACTUALIZADO":
                    System.out.println("[IndexadorWorker] Indexando hecho: " + hechoId);
                    indexadorService.indexar(hechoId);
                    System.out.println("[IndexadorWorker] Hecho indexado exitosamente: " + hechoId);
                    break;

                case "PDI_PROCESADO":
                    // Cuando un PDI es procesado (con OCR y etiquetas), re-indexamos el hecho completo
                    Integer pdiId = event.has("pdiId") ? event.get("pdiId").asInt() : null;
                    System.out.println("[IndexadorWorker] Re-indexando hecho " + hechoId + " por PDI procesado: " + pdiId);
                    indexadorService.indexar(hechoId);
                    System.out.println("[IndexadorWorker] Hecho re-indexado exitosamente: " + hechoId);
                    break;

                case "HECHO_BORRADO":
                    System.out.println("[IndexadorWorker] Marcando hecho como borrado: " + hechoId);
                    indexadorService.marcarComoBorrado(hechoId);
                    System.out.println("[IndexadorWorker] Hecho marcado como borrado: " + hechoId);
                    break;

                default:
                    System.err.println("[IndexadorWorker] Tipo de evento desconocido: " + eventType);
            }

        } catch (Exception e) {
            System.err.println("[IndexadorWorker] Error procesando evento: " + mensaje);
            e.printStackTrace();
            // En un sistema real, aquí se podría enviar a una Dead Letter Queue
            // o implementar retry logic con Spring AMQP
        }
    }
}
