package ar.edu.utn.dds.k3003.telegram.states;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager.Context;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CrearPdiLugarState implements ConversationState {

    @Override
    public String handle(Context context, String text, ApiClientService apiClient) {
        context.payload.put("lugar", text);
        // El momento se autogenera, como solicitado.
        context.payload.put("momento", LocalDateTime.now().withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return "Lugar guardado y momento autogenerado. Ahora, por favor, enviá el contenido o texto del PdI:";
    }
}
