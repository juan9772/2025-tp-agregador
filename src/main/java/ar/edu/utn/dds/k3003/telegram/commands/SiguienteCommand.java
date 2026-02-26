package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class SiguienteCommand implements Command {

    private final ApiClientService apiClientService;
    private final ConversationManager conversationManager;

    public SiguienteCommand(ApiClientService apiClientService, ConversationManager conversationManager) {
        this.apiClientService = apiClientService;
        this.conversationManager = conversationManager;
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
        ConversationManager.Context context = conversationManager.getContext(chatId);
        String lastQuery = (String) context.payload.get("last_search_query");
        Integer lastPage = (Integer) context.payload.get("last_search_page");
        Integer totalPages = (Integer) context.payload.get("last_search_total_pages");

        if (lastQuery == null || lastPage == null || totalPages == null) {
            bot.executeSend(chatId, "No hay una búsqueda anterior para mostrar la página siguiente. Realice una búsqueda primero con /buscar.");
            return;
        }

        int nextPage = lastPage + 1;
        if (nextPage >= totalPages) {
            bot.executeSend(chatId, "Ya estás en la última página.");
            return;
        }

        try {
            Map<String, Object> pageResult = apiClientService.buscar(lastQuery, nextPage, 10);

            if (pageResult == null) {
                log.error("La API de búsqueda devolvió un resultado nulo para la consulta: {}", lastQuery);
                bot.executeSend(chatId, "Error: No se pudo obtener una respuesta del servicio de búsqueda.");
                return;
            }

            List<Map<String, Object>> resultados = (List<Map<String, Object>>) pageResult.get("content");
            long totalElements = Optional.ofNullable((Number) pageResult.get("totalElements")).map(Number::longValue).orElse(0L);

            if (resultados == null || resultados.isEmpty()) {
                bot.executeSend(chatId, "No se encontraron más resultados.");
                return;
            }

            // Actualizar el estado de la búsqueda en la conversación
            context.payload.put("last_search_page", nextPage);

            StringBuilder sb = new StringBuilder("Resultados de la búsqueda:\n");
            for (Map<String, Object> hecho : resultados) {
                sb.append("\n- *Hecho:* ").append(hecho.get("displayNombre"))
                  .append("\n  *ID:* ").append(hecho.get("id"));

                List<String> colecciones = (List<String>) hecho.get("colecciones");
                if (colecciones != null && !colecciones.isEmpty()) {
                    sb.append("\n  *Colecciones:* ").append(String.join(", ", colecciones));
                }
                sb.append("\n");
            }
            sb.append("\n---\nPágina ").append(nextPage + 1).append(" de ").append(totalPages)
              .append(" (Total: ").append(totalElements).append(" resultados)");

            bot.executeSend(chatId, sb.toString());

        } catch (Exception e) {
            bot.executeSend(chatId, "Error al realizar la búsqueda: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getCommand() {
        return "/siguiente";
    }

    @Override
    public String getHelp() {
        return "Muestra la siguiente página de resultados de la última búsqueda.";
    }
}
