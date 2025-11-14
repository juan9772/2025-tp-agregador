package ar.edu.utn.dds.k3003.telegram.states.flow;

import ar.edu.utn.dds.k3003.telegram.states.ConversationState;

public interface StateFlowOrchestrator {

    String CREAR_HECHO_FLOW = "crear_hecho";
    String CREAR_PDI_FLOW = "crear_pdi";
    String CREAR_SOLICITUD_FLOW = "crear_solicitud";
    String AGREGAR_FUENTE_FLOW = "agregar_fuente";


    ConversationState getInitialState(String flowName);

    ConversationState getNextState(String flowName, ConversationState currentState);
}
