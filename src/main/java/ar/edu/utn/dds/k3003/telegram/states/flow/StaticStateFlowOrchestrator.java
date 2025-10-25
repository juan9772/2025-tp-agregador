package ar.edu.utn.dds.k3003.telegram.states.flow;

import ar.edu.utn.dds.k3003.telegram.states.*;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class StaticStateFlowOrchestrator implements StateFlowOrchestrator {

    private final Map<String, Supplier<ConversationState>> initialStates;
    private final Map<Class<? extends ConversationState>, Supplier<ConversationState>> flowTransitions;

    public StaticStateFlowOrchestrator() {
        initialStates = Map.of(
                CREAR_HECHO_FLOW, CrearTituloState::new,
                CREAR_PDI_FLOW, CrearPdiUrlState::new,
                CREAR_SOLICITUD_FLOW, CrearSolicitudDescripcionState::new
        );

        flowTransitions = Map.of(
                // Crear Hecho Flow
                CrearTituloState.class, CrearColeccionState::new,
                CrearColeccionState.class, () -> new ConfirmarState(
                        (apiSvc, payload) -> {
                            payload.put("id", "");
                            return apiSvc.crearHecho(payload);
                        },
                        (result) -> "Hecho creado con ID: " + result.getOrDefault("id", "(desconocido)"),
                        "Creación cancelada."
                ),

                // Crear PDI Flow (Corregido y Centralizado)
                CrearPdiUrlState.class, CrearPdiDescripcionState::new,
                CrearPdiDescripcionState.class, CrearPdiLugarState::new,
                CrearPdiLugarState.class, CrearPdiContenidoState::new,
                CrearPdiContenidoState.class, () -> new ConfirmarState(
                        (apiSvc, payload) -> {
                            payload.put("id", "");
                            return apiSvc.crearPdi(payload);
                        },
                        (result) -> "Punto de Interés creado con éxito y asociado al hecho.",
                        "Creación de PdI cancelada."
                ),

                // Crear Solicitud Flow
                CrearSolicitudDescripcionState.class, () -> new ConfirmarState(
                        (apiSvc, payload) -> {
                            payload.put("id", "");
                            payload.put("estado", "CREADA");
                            return apiSvc.crearSolicitud(payload);
                        },
                        (result) -> "Solicitud de borrado creada con éxito con ID: " + result.getOrDefault("id", "(desconocido)"),
                        "Creación de solicitud cancelada."
                )
        );
    }

    @Override
    public ConversationState getInitialState(String flowName) {
        Supplier<ConversationState> supplier = initialStates.get(flowName);
        return (supplier != null) ? supplier.get() : new IdleState();
    }

    @Override
    public ConversationState getNextState(String flowName, ConversationState currentState) {
        if (currentState instanceof ConfirmarState) {
            return new IdleState();
        }

        Supplier<ConversationState> supplier = flowTransitions.get(currentState.getClass());
        return (supplier != null) ? supplier.get() : new IdleState();
    }
}
