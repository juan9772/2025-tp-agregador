package ar.edu.utn.dds.k3003.telegram.states;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager;

public class CrearFuenteNombreState implements ConversationState {
    @Override
    public String handle(ConversationManager.Context context, String message, ApiClientService apiClient) {
        context.payload.put("nombre", message);
        return "Nombre de la fuente guardado. Ahora, por favor, ingrese el endpoint de la fuente:";
    }
}
