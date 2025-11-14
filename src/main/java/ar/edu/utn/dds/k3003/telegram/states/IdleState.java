package ar.edu.utn.dds.k3003.telegram.states;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager;

public class IdleState implements ConversationState {

    @Override
    public String handle(ConversationManager.Context context, String message, ApiClientService apiClient) {
        // En estado Idle, no se hace nada con los mensajes genéricos.
        // El manejo de comandos se hace antes de llegar aquí.
        return "Comando no reconocido. Escribe /help para ver la lista de comandos.";
    }
}
