package ar.edu.utn.dds.k3003.telegram.states;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager.Context;

public class CrearColeccionState implements ConversationState {

    @Override
    public String handle(Context context, String text, ApiClientService apiClient) {
        // La API espera el campo ID, aunque esté vacío.
        context.payload.put("nombreColeccion", text);

        String titulo = (String) context.payload.getOrDefault("titulo", "(sin título)");
        String colec = (String) context.payload.getOrDefault("nombreColeccion", "(sin colección)");

        return String.format(
                "Confirmá la creación del hecho:\nTítulo: %s\nColección: %s\n\nEscribí 'si' para confirmar o 'no' para cancelar.",
                titulo, colec
        );
    }
}
