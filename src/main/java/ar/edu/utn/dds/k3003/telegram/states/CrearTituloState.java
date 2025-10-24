package ar.edu.utn.dds.k3003.telegram.states;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager.Context;

public class CrearTituloState implements ConversationState {

    @Override
    public String handle(Context context, String text, ApiClientService apiClient) {
        context.payload.put("id", "");
        context.payload.put("titulo", text);
        context.state = new CrearColeccionState();
        return "Perfecto. Ahora indicá el nombre de la colección a la que pertenece el hecho:";
    }
}
