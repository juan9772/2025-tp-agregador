package ar.edu.utn.dds.k3003.telegram.states;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager.Context;

public class CrearPdiUrlState implements ConversationState {

    @Override
    public String handle(Context context, String text, ApiClientService apiClient) {
        context.payload.put("imagen_url", text);
        return "URL de imagen guardada. Ahora, por favor, enviá una descripción para el PdI:";
    }
}
