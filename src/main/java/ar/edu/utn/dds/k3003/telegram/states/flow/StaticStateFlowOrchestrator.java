package ar.edu.utn.dds.k3003.telegram.states.flow;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager;
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
                CrearColeccionState.class, ConfirmarState::new,

                // Crear PDI Flow (Corregido y Centralizado)
                CrearPdiUrlState.class, CrearPdiDescripcionState::new,
                CrearPdiDescripcionState.class, CrearPdiLugarState::new,
                CrearPdiLugarState.class, CrearPdiContenidoState::new,
                CrearPdiContenidoState.class, () -> new ConversationState() {
                    @Override
                    public String handle(ConversationManager.Context context, String message, ApiClientService apiClient) {
                        if ("si".equalsIgnoreCase(message)) {
                            try {
                                Map<String, Object> payload = context.payload;
                                payload.put("id", "");
                                apiClient.crearPdi(payload);
                                return "Punto de Interés creado con éxito y asociado al hecho.";
                            } catch (Exception e) {
                                return "Error al crear el PdI: " + e.getMessage();
                            }
                        } else {
                            return "Creación de PdI cancelada.";
                        }
                    }
                },

                // Crear Solicitud Flow
                CrearSolicitudDescripcionState.class, () -> new ConversationState() {
                    @Override
                    public String handle(ConversationManager.Context context, String message, ApiClientService apiClient) {
                        if ("si".equalsIgnoreCase(message)) {
                            try {
                                Map<String, Object> payload = context.payload;
                                payload.put("id", "");
                                payload.put("estado", "CREADA");
                                Map<String, Object> result = apiClient.crearSolicitud(payload);
                                return "Solicitud de borrado creada con éxito con ID: " + result.getOrDefault("id", "(desconocido)");
                            } catch (Exception e) {
                                return "Error al crear la solicitud: " + e.getMessage();
                            }
                        } else {
                            return "Creación de solicitud cancelada.";
                        }
                    }
                }
        );
    }

    @Override
    public ConversationState getInitialState(String flowName) {
        Supplier<ConversationState> supplier = initialStates.get(flowName);
        return (supplier != null) ? supplier.get() : new IdleState();
    }

    @Override
    public ConversationState getNextState(String flowName, ConversationState currentState) {
        Supplier<ConversationState> supplier = flowTransitions.get(currentState.getClass());
        return (supplier != null) ? supplier.get() : new IdleState();
    }
}
