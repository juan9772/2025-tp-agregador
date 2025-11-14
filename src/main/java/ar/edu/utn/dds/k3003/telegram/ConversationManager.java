package ar.edu.utn.dds.k3003.telegram;

import ar.edu.utn.dds.k3003.busqueda.services.IndexadorService;
import ar.edu.utn.dds.k3003.telegram.states.ConversationState;
import ar.edu.utn.dds.k3003.telegram.states.IdleState;
import ar.edu.utn.dds.k3003.telegram.states.flow.StateFlowOrchestrator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConversationManager {

    public static class Context {
        public String flowName;
        public ConversationState state = new IdleState();
        public Map<String, Object> payload = new ConcurrentHashMap<>();
        public Instant updated = Instant.now();
        public IndexadorService indexadorService; // <-- CAMPO NUEVO
    }

    private final Map<Long, Context> contexts = new ConcurrentHashMap<>();
    private final StateFlowOrchestrator orchestrator;
    private final Duration TTL = Duration.ofMinutes(15);

    public ConversationManager(StateFlowOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    private void startFlow(long chatId, String flowName, Map<String, Object> initialPayload, IndexadorService indexadorService) {
        Context c = new Context();
        c.flowName = flowName;
        c.state = orchestrator.getInitialState(c.flowName);
        c.payload.putAll(initialPayload);
        c.updated = Instant.now();
        c.indexadorService = indexadorService; // <-- GUARDAR EL SERVICIO
        contexts.put(chatId, c);
    }

    public void startCreating(long chatId, IndexadorService indexadorService) {
        startFlow(chatId, StateFlowOrchestrator.CREAR_HECHO_FLOW, Collections.emptyMap(), indexadorService);
    }

    public void startAgregarPdi(long chatId, String hechoId, IndexadorService indexadorService) {
        startFlow(chatId, StateFlowOrchestrator.CREAR_PDI_FLOW, Map.of("hecho_id", hechoId), indexadorService);
    }

    public void startCrearSolicitud(long chatId, String hechoId) {
        startFlow(chatId, StateFlowOrchestrator.CREAR_SOLICITUD_FLOW, Map.of("hecho_id", hechoId), null); // No se necesita indexador aquí
    }

    public void startAgregarFuente(long chatId) {
        startFlow(chatId, StateFlowOrchestrator.AGREGAR_FUENTE_FLOW, Collections.emptyMap(), null);
    }

    public ConversationState getState(long chatId) {
        cleanupExpired();
        Context c = contexts.get(chatId);
        return c == null ? new IdleState() : c.state;
    }

    public Context getContext(long chatId) {
        cleanupExpired();
        return contexts.computeIfAbsent(chatId, k -> new Context());
    }

    public String handle(long chatId, String text, ApiClientService apiClient) {
        cleanupExpired();
        Context c = contexts.computeIfAbsent(chatId, k -> new Context());
        c.updated = Instant.now();

        // Pasar el indexador al estado
        String response = c.state.handle(c, text, apiClient);

        c.state = orchestrator.getNextState(c.flowName, c.state);

        return response;
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        contexts.entrySet().removeIf(e -> Duration.between(e.getValue().updated, now).compareTo(TTL) > 0);
    }
}
