package ar.edu.utn.dds.k3003.telegram.states;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager;

import java.util.Map;

public class ConfirmarState implements ConversationState {

    @Override
    public String handle(ConversationManager.Context context, String message, ApiClientService apiClient) {
        if ("si".equalsIgnoreCase(message)) {
            try {
                Map<String, Object> resultado = apiClient.crearHecho(context.payload);
                String hechoId = (String) resultado.get("id");

                if (context.indexadorService != null && hechoId != null) {
                    context.indexadorService.indexar(hechoId);
                    return "Hecho creado e indexado con ID: " + hechoId;
                } else {
                    return "Hecho creado con ID: " + hechoId;
                }

            } catch (Exception e) {
                return "Error al crear el hecho: " + e.getMessage();
            }
        } else {
            return "Creación cancelada.";
        }
    }
}
