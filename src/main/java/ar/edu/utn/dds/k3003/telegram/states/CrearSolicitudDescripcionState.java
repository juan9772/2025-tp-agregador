package ar.edu.utn.dds.k3003.telegram.states;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager.Context;

public class CrearSolicitudDescripcionState implements ConversationState {

    @Override
    public String handle(Context context, String text, ApiClientService apiClient) {
        context.payload.put("descripcion", text);

        // Corregido: Leer el valor como objeto y convertirlo a String de forma segura.
        String hechoId = String.valueOf(context.payload.getOrDefault("hechoId", "(desconocido)"));
        String desc = (String) context.payload.getOrDefault("descripcion", "(sin descripción)");

        return "Confirmá la solicitud de borrado para el hecho " + hechoId + ":\nMotivo: " + desc + "\nEscribí 'si' para confirmar o 'no' para cancelar.";
    }
}
