package ar.edu.utn.dds.k3003.telegram.states;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager;

public class CrearDescripcionState implements ConversationState {

    @Override
    public String handle(ConversationManager.Context context, String message, ApiClientService apiClient) {
        context.payload.put("descripcion", message);
        return "Hecho a crear:\n" +
                "Título: " + context.payload.get("titulo") + "\n" +
                "Descripción: " + context.payload.get("descripcion") + "\n" +
                "¿Confirmar? (si/no)";
    }
}
