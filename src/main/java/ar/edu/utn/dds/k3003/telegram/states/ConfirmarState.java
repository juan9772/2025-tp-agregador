package ar.edu.utn.dds.k3003.telegram.states;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager.Context;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfirmarState implements ConversationState {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarState.class);
    private final BiFunction<ApiClientService, Map<String, Object>, Map<String, Object>> action;
    private final Function<Map<String, Object>, String> successMessageBuilder;
    private final String cancellationMessage;

    public ConfirmarState(BiFunction<ApiClientService, Map<String, Object>, Map<String, Object>> action, Function<Map<String, Object>, String> successMessageBuilder, String cancellationMessage) {
        this.action = action;
        this.successMessageBuilder = successMessageBuilder;
        this.cancellationMessage = cancellationMessage;
    }

    @Override
    public String handle(Context context, String text, ApiClientService apiClient) {
        if (text.equalsIgnoreCase("si") || text.equalsIgnoreCase("s")) {
            try {
                log.info("Confirmación recibida. Ejecutando acción del flujo '{}' con payload: {}", context.flowName, context.payload);
                Map<String, Object> result = action.apply(apiClient, context.payload);
                context.state = new IdleState();
                if (result == null) {
                    log.error("La acción en el servidor falló (devolvió null). El payload enviado fue: {}", context.payload);
                    return "Error: La operación en el servidor falló.";
                }
                log.info("La acción en el servidor fue exitosa. Resultado: {}", result);
                return successMessageBuilder.apply(result);
            } catch (Exception e) {
                log.error("La acción en el servidor lanzó una excepción. Payload: {}. Excepción: {}", context.payload, e.getMessage(), e);
                context.state = new IdleState();
                return "Error: La operación en el servidor falló. Motivo: " + e.getMessage();
            }
        } else {
            log.info("Creación cancelada por el usuario para el flujo '{}'.", context.flowName);
            context.state = new IdleState();
            return cancellationMessage;
        }
    }
}
