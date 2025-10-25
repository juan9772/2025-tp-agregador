package ar.edu.utn.dds.k3003.telegram.states;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager.Context;

public class CrearPdiContenidoState implements ConversationState {

    @Override
    public String handle(Context context, String text, ApiClientService apiClient) {
        context.payload.put("contenido", text);

        // Safely get all values for the confirmation message.
        String hechoId = String.valueOf(context.payload.getOrDefault("hecho_id", "(desconocido)"));
        String imageUrl = (String) context.payload.getOrDefault("imagen_url", "(sin URL)");
        String desc = (String) context.payload.getOrDefault("descripcion", "(sin descripción)");
        String lugar = (String) context.payload.getOrDefault("lugar", "(sin lugar)");
        String momento = (String) context.payload.getOrDefault("momento", "(sin momento)");
        String contenido = (String) context.payload.getOrDefault("contenido", "(sin contenido)");

        return String.format(
                "Confirmá la creación del PdI para el hecho %s:\n\nImagen URL: %s\nDescripción: %s\nLugar: %s\nMomento: %s\nContenido: %s\n\nEscribí 'si' para confirmar o 'no' para cancelar.",
                hechoId, imageUrl, desc, lugar, momento, contenido
        );
    }
}
