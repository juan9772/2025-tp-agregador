package ar.edu.utn.dds.k3003.telegram.states;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager.Context;

public class CrearColeccionState implements ConversationState {
    @Override
    public String handle(Context context, String text, ApiClientService apiClient) {
        context.payload.put("nombreColeccion", text);
        context.state = new CrearConfirmarState();
        String titulo = (String) context.payload.getOrDefault("titulo", "(sin título)");
        String colec = (String) context.payload.getOrDefault("nombreColeccion", "(sin colección)");
        return "Confirmá creación:\nTítulo: " + titulo + "\nColección: " + colec + "\nEscribí 'si' para confirmar o 'no' para cancelar.";
    }
}
