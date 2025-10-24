package ar.edu.utn.dds.k3003.telegram;

import ar.edu.utn.dds.k3003.telegram.states.ConversationState;
import ar.edu.utn.dds.k3003.telegram.states.CrearTituloState;
import ar.edu.utn.dds.k3003.telegram.states.IdleState;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class ConversationManager {

    // El enum State ya no es necesario, lo reemplazamos por el patrón State.

    public static class Context {
        public ConversationState state = new IdleState(); // Inicia en el estado IDLE
        public Map<String, Object> payload = new ConcurrentHashMap<>();
        public Instant updated = Instant.now();
    }

    private final Map<Long, Context> contexts = new ConcurrentHashMap<>();
    private final Duration TTL = Duration.ofMinutes(15);

    public void startCreating(long chatId) {
        Context c = new Context();
        c.state = new CrearTituloState(); // Inicia el flujo con el estado para pedir el título
        c.updated = Instant.now();
        contexts.put(chatId, c);
    }

    public void startAgregarPdi(long chatId, String hechoId) {
        // TODO: Implementar el flujo de AGREGAR_PDI con el patrón State
        // Por ahora, lo dejamos como estaba para no romper la funcionalidad.
        Context c = new Context();
        // c.state = new AgregarPdiDescripcionState(); // (a implementar)
        c.payload.put("hecho_id", hechoId);
        c.updated = Instant.now();
        contexts.put(chatId, c);
    }

    public void startCrearSolicitud(long chatId, String hechoId) {
        // TODO: Implementar el flujo de SOLICITUD con el patrón State
        Context c = new Context();
        // c.state = new SolicitarBorradoDescripcionState(); // (a implementar)
        c.payload.put("hechoId", hechoId);
        c.updated = Instant.now();
        contexts.put(chatId, c);
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

        // La magia del patrón State: delegamos el comportamiento al estado actual.
        // El switch gigante ha desaparecido.
        return c.state.handle(c, text, apiClient);
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        contexts.entrySet().removeIf(e -> Duration.between(e.getValue().updated, now).compareTo(TTL) > 0);
    }
}
