package ar.edu.utn.dds.k3003.telegram.states;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager.Context;
import java.util.Map;

public class CrearConfirmarState implements ConversationState {

    @Override
    public String handle(Context context, String text, ApiClientService apiClient) {
        if (text.equalsIgnoreCase("si") || text.equalsIgnoreCase("s")) {
            Map<String, Object> payload = context.payload;
            Map<String, Object> created = apiClient.crearHecho(payload);
            context.state = new IdleState(); // Volver al estado inicial
            if (created == null) {
                return "Error al crear el hecho en el servidor.";
            }
            return "Hecho creado con ID: " + created.getOrDefault("id", "(desconocido)");
        } else {
            context.state = new IdleState(); // Volver al estado inicial
            return "Creación cancelada.";
        }
    }
}
