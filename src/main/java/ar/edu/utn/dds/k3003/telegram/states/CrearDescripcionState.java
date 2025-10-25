package ar.edu.utn.dds.k3003.telegram.states;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager.Context;

public class CrearDescripcionState implements ConversationState {

    @Override
    public String handle(Context context, String text, ApiClientService apiClient) {
        context.payload.put("descripcion", text);
        return "Descripción guardada. Ahora, ¿en qué colección lo guardamos?";
    }
}
