package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager;
import ar.edu.utn.dds.k3003.telegram.MetricasService;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class BuscarCommand implements Command {

    private final ApiClientService apiClientService;
    private final ConversationManager conversationManager;
    private final MetricasService metricasService;

    public BuscarCommand(ApiClientService apiClientService, ConversationManager conversationManager, MetricasService metricasService) {
        this.apiClientService = apiClientService;
        this.conversationManager = conversationManager;
        this.metricasService = metricasService;
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
        if (args == null || args.isEmpty()) {
            bot.executeSend(chatId, "Uso: /buscar <palabras> [tag:tag1] [page:0]");
            return;
        }

        // Expresión regular para capturar texto entre comillas, tags y paginación
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\\S+)");
        Matcher matcher = pattern.matcher(args);

        List<String> parts = new ArrayList<>();
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // Texto entre comillas
                parts.add(matcher.group(1));
            } else {
                // Texto sin comillas (tags, paginación, etc.)
                parts.add(matcher.group(2));
            }
        }

        int page = 0;
        StringBuilder queryBuilder = new StringBuilder();

        for (String part : parts) {
            String cleanedPart = part.replace(",", "").trim(); // Limpiar comas
            if (cleanedPart.toLowerCase().startsWith("page:")) {
                try {
                    page = Integer.parseInt(cleanedPart.substring(5));
                } catch (NumberFormatException e) {
                    // Ignorar si el formato es incorrecto
                }
            } else {
                if (queryBuilder.length() > 0) {
                    queryBuilder.append(" ");
                }
                queryBuilder.append(cleanedPart);
            }
        }

        String finalQuery = queryBuilder.toString();

        long startTime = System.nanoTime();
        try {
            // Llamar a la API de búsqueda con tamaño de página 10
            Map<String, Object> pageResult = apiClientService.buscar(finalQuery, page, 10);

            if (pageResult == null) {
                log.error("La API de búsqueda devolvió un resultado nulo para la consulta: {}", finalQuery);
                bot.executeSend(chatId, "Error: No se pudo obtener una respuesta del servicio de búsqueda.");
                return;
            }

            // Procesar la respuesta con valores por defecto para evitar NPE
            List<Map<String, Object>> resultados = (List<Map<String, Object>>) pageResult.get("content");
            int totalPages = Optional.ofNullable((Integer) pageResult.get("total_pages")).orElse(1);
            long totalElements = Optional.ofNullable((Number) pageResult.get("total_elements")).map(Number::longValue).orElse(0L);

            if (resultados == null || resultados.isEmpty()) {
                bot.executeSend(chatId, "No se encontraron resultados para su búsqueda.");
                return;
            }

            // Guardar el estado de la búsqueda en la conversación
            ConversationManager.Context context = conversationManager.getContext(chatId);
            context.payload.put("last_search_query", finalQuery);
            context.payload.put("last_search_page", page);
            context.payload.put("last_search_total_pages", totalPages);


            StringBuilder sb = new StringBuilder("Resultados de la búsqueda:\n");
            for (Map<String, Object> hecho : resultados) {
                sb.append("\n- *Hecho:* ").append(hecho.get("display_nombre"))
                  .append("\n  *ID:* ").append(hecho.get("id"));

                List<String> colecciones = (List<String>) hecho.get("colecciones");
                if (colecciones != null && !colecciones.isEmpty()) {
                    sb.append("\n  *Colecciones:* ").append(String.join(", ", colecciones));
                }
                sb.append("\n");
            }
            sb.append("\n---\nPágina ").append(page + 1).append(" de ").append(totalPages)
              .append(" (Total: ").append(totalElements).append(" resultados)");

            bot.executeSend(chatId, sb.toString());

        } catch (Exception e) {
            bot.executeSend(chatId, "Error al realizar la búsqueda: " + e.getMessage());
            e.printStackTrace();
        } finally {
            long duration = System.nanoTime() - startTime;
            metricasService.registrarDuracionBusqueda(duration, TimeUnit.NANOSECONDS);
            metricasService.incrementarBusquedas();
        }
    }

    @Override
    public String getCommand() {
        return "/buscar";
    }

    @Override
    public String getHelp() {
        return "Uso: /buscar <palabras> [tag:tag1] [page:0] - Busca hechos por palabras clave y tags opcionales.";
    }
}
