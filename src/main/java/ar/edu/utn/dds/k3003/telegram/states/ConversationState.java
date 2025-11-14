package ar.edu.utn.dds.k3003.telegram.states;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager;

public interface ConversationState {

    String handle(ConversationManager.Context context, String message, ApiClientService apiClient);
}
