package ar.edu.utn.dds.k3003.telegram.states;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager.Context;

public class IdleState implements ConversationState {
    @Override
    public String handle(Context context, String text, ApiClientService apiClient) {
        return "No hay una operación en curso. Usá /crear para crear un hecho, /agregarpdi <hechoId> o /solicitarborrado <hechoId>.";
    }
}
