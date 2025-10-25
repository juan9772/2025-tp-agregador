package ar.edu.utn.dds.k3003.telegram;

import ar.edu.utn.dds.k3003.telegram.states.ConversationState;
import ar.edu.utn.dds.k3003.telegram.states.IdleState;
import ar.edu.utn.dds.k3003.telegram.states.flow.StateFlowOrchestrator;
import org.springframework.stereotype.Component;
import ar.edu.utn.dds.k3003.telegram.ApiClientService;

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
    }

    private final Map<Long, Context> contexts = new ConcurrentHashMap<>();
    private final StateFlowOrchestrator orchestrator;
    private final Duration TTL = Duration.ofMinutes(15);

    public ConversationManager(StateFlowOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    private void startFlow(long chatId, String flowName, Map<String, Object> initialPayload) {
        Context c = new Context();
        c.flowName = flowName;
        c.state = orchestrator.getInitialState(c.flowName);
        c.payload.putAll(initialPayload);
        c.updated = Instant.now();
        contexts.put(chatId, c);
    }

    public void startCreating(long chatId) {
        startFlow(chatId, StateFlowOrchestrator.CREAR_HECHO_FLOW, Collections.emptyMap());
    }

    public void startAgregarPdi(long chatId, String hechoId) {
        startFlow(chatId, StateFlowOrchestrator.CREAR_PDI_FLOW, Map.of("hecho_id", hechoId));
    }

    public void startCrearSolicitud(long chatId, String hechoId) {
        // Corregido: Usar snake_case para consistencia.
        startFlow(chatId, StateFlowOrchestrator.CREAR_SOLICITUD_FLOW, Map.of("hecho_id", hechoId));
    }

    public ConversationState getState(long chatId) {
        cleanupExpired();
        Context c = contexts.get(chatId);
        return c == null ? new IdleState() : c.state;
    }

    public String handle(long chatId, String text, ApiClientService apiClient) {
        cleanupExpired();
        Context c = contexts.computeIfAbsent(chatId, k -> new Context());
        c.updated = Instant.now();

        String response = c.state.handle(c, text, apiClient);

        c.state = orchestrator.getNextState(c.flowName, c.state);

        return response;
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        contexts.entrySet().removeIf(e -> Duration.between(e.getValue().updated, now).compareTo(TTL) > 0);
    }
}
