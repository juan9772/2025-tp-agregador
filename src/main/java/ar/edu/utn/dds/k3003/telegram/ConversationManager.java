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
        AGREGAR_PDI_DESCRIPCION,
        AGREGAR_PDI_LUGAR,
        AGREGAR_PDI_MOMENTO,
        AGREGAR_PDI_CONTENIDO,
        AGREGAR_PDI_IMAGEN_URL,
        AGREGAR_PDI_CONFIRMAR,
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
        c.state = State.AGREGAR_PDI_DESCRIPCION;
        c.payload.put("hecho_id", hechoId);
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
                c.payload.put("id", "");
                c.payload.put("titulo", text);
                c.state = State.CREAR_COLECCION;
                return "Perfecto. Ahora indicá el nombre de la colección a la que pertenece el hecho:";
            }
            case CREAR_COLECCION -> {
                c.payload.put("nombreColeccion", text);
                c.state = State.CREAR_CONFIRMAR;
                String titulo = (String) c.payload.getOrDefault("titulo", "(sin título)");
                String colec = (String) c.payload.getOrDefault("nombreColeccion", "(sin colección)");
                return "Confirmá creación:\nTítulo: " + titulo + "\nColección: " + colec + "\nEscribí 'si' para confirmar o 'no' para cancelar.";
                //return "Ok. Ahora escribí una breve descripción:";
            }
//            case CREAR_DESCRIPCION -> {
//                c.payload.put("descripcion", text);
//                c.state = State.CREAR_CONFIRMAR;
//                String titulo = (String) c.payload.getOrDefault("titulo", "(sin título)");
//                String colec = (String) c.payload.getOrDefault("nombreColeccion", "(sin colección)");
//                String desc = (String) c.payload.getOrDefault("descripcion", "(sin descripción)");
//                return "Confirmá creación:\nTítulo: " + titulo + "\nColección: " + colec + "\nDescripción: " + desc + "\nEscribí 'si' para confirmar o 'no' para cancelar.";
//            }
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
            case AGREGAR_PDI_DESCRIPCION -> {
                c.payload.put("descripcion", text);
                c.state = State.AGREGAR_PDI_LUGAR;
                return "Descripción recibida. Ahora ingresá el lugar:";
            }
            case AGREGAR_PDI_LUGAR -> {
                c.payload.put("lugar", text);
                c.state = State.AGREGAR_PDI_MOMENTO;
                return "Lugar recibido. Ahora ingresá el momento (formato YYYY-MM-DDTHH:MM:SS):";
            }
            case AGREGAR_PDI_MOMENTO -> {
                c.payload.put("momento", text);
                c.state = State.AGREGAR_PDI_CONTENIDO;
                return "Momento recibido. Ahora ingresá el contenido:";
            }
            case AGREGAR_PDI_CONTENIDO -> {
                c.payload.put("contenido", text);
                c.state = State.AGREGAR_PDI_IMAGEN_URL;
                return "Contenido recibido. Ahora ingresá la URL de la imagen:";
            }
            case AGREGAR_PDI_IMAGEN_URL -> {
                c.payload.put("imagen_url", text);
                c.state = State.AGREGAR_PDI_CONFIRMAR;
                String desc = (String) c.payload.getOrDefault("descripcion", "(sin descripción)");
                String lugar = (String) c.payload.getOrDefault("lugar", "(sin lugar)");
                String momento = (String) c.payload.getOrDefault("momento", "(sin momento)");
                String contenido = (String) c.payload.getOrDefault("contenido", "(sin contenido)");
                String img = (String) c.payload.getOrDefault("imagen_url", "(sin imagen)");
                return "Confirmá la creación del PdI:\n" +
                        "Descripción: " + desc + "\n" +
                        "Lugar: " + lugar + "\n" +
                        "Momento: " + momento + "\n" +
                        "Contenido: " + contenido + "\n" +
                        "Imagen URL: " + img + "\n" +
                        "Escribí 'si' para confirmar o 'no' para cancelar.";
            }
            case AGREGAR_PDI_CONFIRMAR -> {
                if (text.equalsIgnoreCase("si") || text.equalsIgnoreCase("s")) {
                    Map<String, Object> pdiPayload = new ConcurrentHashMap<>(c.payload);
                    pdiPayload.put("id", "");
                    Map<String, Object> created = apiClient.crearPdi(pdiPayload);
                    contexts.remove(chatId);
                    if (created == null) return "Error al crear el PdI en el procesador.";
                    return "PdI creado con ID: " + created.getOrDefault("id", "(desconocido)");
                } else {
                    contexts.remove(chatId);
                    return "Creación de PdI cancelada.";
                }
            }
            case SOLICITUD_DESCRIPCION -> {
                String hechoId = String.valueOf(c.payload.get("hechoId"));
                Map<String, Object> solPayload = new ConcurrentHashMap<>();
                solPayload.put("id", "");
                solPayload.put("hechoId", hechoId);
                solPayload.put("descripcion", text);
                solPayload.put("estado", "CREADA");
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
