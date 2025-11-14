package ar.edu.utn.dds.k3003.telegram.states;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager;

import java.util.Map;

public class CrearFuenteEndpointState implements ConversationState {
    @Override
    public String handle(ConversationManager.Context context, String message, ApiClientService apiClient) {
        context.payload.put("endpoint", message);
        try {
            Map<String, Object> payload = context.payload;
            apiClient.crearFuente(payload);
            return "Fuente agregada con éxito.";
        } catch (Exception e) {
            return "Error al agregar la fuente: " + e.getMessage();
        }
    }
}
