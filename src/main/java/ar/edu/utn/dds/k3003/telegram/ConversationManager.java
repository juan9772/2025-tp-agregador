package ar.edu.utn.dds.k3003.telegram;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class ConversationManager {

    public enum State {
        IDLE,
        CREAR_TITULO,
        CREAR_COLECCION,
        CREAR_DESCRIPCION,
        CREAR_CONFIRMAR,
        AGREGAR_PDI_URL,
        AGREGAR_PDI_DESC,
        SOLICITUD_DESCRIPCION
    }

    public static class Context {
        public State state = State.IDLE;
        public Map<String, Object> payload = new ConcurrentHashMap<>();
        public Instant updated = Instant.now();
    }

    private final Map<Long, Context> contexts = new ConcurrentHashMap<>();
    private final Duration TTL = Duration.ofMinutes(15);

    public void startCreating(long chatId) {
        Context c = new Context();
        c.state = State.CREAR_TITULO;
        c.updated = Instant.now();
        contexts.put(chatId, c);
    }

    public void startAgregarPdi(long chatId, String hechoId) {
        Context c = new Context();
        c.state = State.AGREGAR_PDI_URL;
        c.payload.put("hechoId", hechoId);
        c.updated = Instant.now();
        contexts.put(chatId, c);
    }

    public void startCrearSolicitud(long chatId, String hechoId) {
        Context c = new Context();
        c.state = State.SOLICITUD_DESCRIPCION;
        c.payload.put("hechoId", hechoId);
        c.updated = Instant.now();
        contexts.put(chatId, c);
    }

    public State getState(long chatId) {
        cleanupExpired();
        Context c = contexts.get(chatId);
        return c == null ? State.IDLE : c.state;
    }

    public String handle(long chatId, String text, ApiClientService apiClient) {
        cleanupExpired();
        Context c = contexts.computeIfAbsent(chatId, k -> new Context());
        c.updated = Instant.now();

        switch (c.state) {
            case CREAR_TITULO -> {
                c.payload.put("titulo", text);
                c.state = State.CREAR_COLECCION;
                return "Perfecto. Ahora indicá el nombre de la colección a la que pertenece el hecho:";
            }
            case CREAR_COLECCION -> {
                c.payload.put("nombreColeccion", text);
                c.state = State.CREAR_DESCRIPCION;
                return "Ok. Ahora escribí una breve descripción:";
            }
            case CREAR_DESCRIPCION -> {
                c.payload.put("descripcion", text);
                c.state = State.CREAR_CONFIRMAR;
                String titulo = (String) c.payload.getOrDefault("titulo", "(sin título)");
                String colec = (String) c.payload.getOrDefault("nombreColeccion", "(sin colección)");
                String desc = (String) c.payload.getOrDefault("descripcion", "(sin descripción)");
                return "Confirmá creación:\nTítulo: " + titulo + "\nColección: " + colec + "\nDescripción: " + desc + "\nEscribí 'si' para confirmar o 'no' para cancelar.";
            }
            case CREAR_CONFIRMAR -> {
                if (text.equalsIgnoreCase("si") || text.equalsIgnoreCase("s")) {
                    Map<String, Object> payload = c.payload;
                    Map<String, Object> created = apiClient.crearHecho(payload);
                    contexts.remove(chatId);
                    if (created == null) return "Error al crear el hecho en el servidor.";
                    return "Hecho creado con ID: " + created.getOrDefault("id", "(desconocido)");
                } else {
                    contexts.remove(chatId);
                    return "Creación cancelada.";
                }
            }
            case AGREGAR_PDI_URL -> {
                c.payload.put("pdiUrl", text);
                c.state = State.AGREGAR_PDI_DESC;
                return "Recibido. Ahora escribí una descripción para el PdI:";
            }
            case AGREGAR_PDI_DESC -> {
                c.payload.put("pdiDescripcion", text);
                // llamar crearPdi en la API
                String hechoId = String.valueOf(c.payload.get("hechoId"));
                Map<String, Object> pdiPayload = new ConcurrentHashMap<>();
                pdiPayload.put("hechoId", hechoId);
                pdiPayload.put("url", c.payload.get("pdiUrl"));
                pdiPayload.put("descripcion", c.payload.get("pdiDescripcion"));
                Map<String, Object> created = apiClient.crearPdi(pdiPayload);
                contexts.remove(chatId);
                if (created == null) return "Error al crear el PdI en el procesador.";
                return "PdI creado con ID: " + created.getOrDefault("id", "(desconocido)");
            }
            case SOLICITUD_DESCRIPCION -> {
                String hechoId = String.valueOf(c.payload.get("hechoId"));
                Map<String, Object> solPayload = new ConcurrentHashMap<>();
                solPayload.put("hechoId", hechoId);
                solPayload.put("descripcion", text);
                Map<String, Object> created = apiClient.crearSolicitud(solPayload);
                contexts.remove(chatId);
                if (created == null) return "Error al crear la solicitud.";
                return "Solicitud creada con ID: " + created.getOrDefault("id", "(desconocido)");
            }
            default -> {
                return "No hay una operación en curso. Usá /crear para crear un hecho, /agregarpdi <hechoId> o /solicitarborrado <hechoId>.";
            }
        }
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        contexts.entrySet().removeIf(e -> Duration.between(e.getValue().updated, now).compareTo(TTL) > 0);
    }
}
