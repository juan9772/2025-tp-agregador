package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BuscarCommand implements Command {

    private final ApiClientService apiClientService;

    public BuscarCommand(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
        if (args == null || args.isEmpty()) {
            bot.executeSend(chatId, "Uso: /buscar <palabras> [tag:tag1] [page:0]");
            return;
        }

        // Extraer página de los argumentos, si existe
        String[] parts = args.split("\\s+");
        int page = 0;
        String query = args;

        for (String part : parts) {
            if (part.toLowerCase().startsWith("page:")) {
                try {
                    page = Integer.parseInt(part.substring(5));
                    query = args.replace(part, "").trim(); // Quitar el page: de la query
                } catch (NumberFormatException e) {
                    // Ignorar si el formato es incorrecto
                }
                break;
            }
        }

        try {
            // Llamar a la API de búsqueda
            Map<String, Object> pageResult = apiClientService.buscar(query, page, 5); // 5 resultados por página

            // Procesar la respuesta
            List<Map<String, Object>> resultados = (List<Map<String, Object>>) pageResult.get("content");
            int totalPages = (int) pageResult.get("totalPages");
            long totalElements = ((Number) pageResult.get("totalElements")).longValue();

            if (resultados == null || resultados.isEmpty()) {
                bot.executeSend(chatId, "No se encontraron resultados para su búsqueda.");
                return;
            }

            StringBuilder sb = new StringBuilder("Resultados de la búsqueda:\n");
            for (Map<String, Object> hecho : resultados) {
                sb.append("\n- *Hecho:* ").append(hecho.get("displayNombre"))
                  .append("\n  *ID:* ").append(hecho.get("id"))
                  .append("\n  *Colecciones:* ").append(String.join(", ", (List<String>)hecho.get("colecciones")))
                  .append("\n");
            }
            sb.append("\n---\nPágina ").append(page + 1).append(" de ").append(totalPages)
              .append(" (Total: ").append(totalElements).append(" resultados)");

            bot.executeSend(chatId, sb.toString());

        } catch (Exception e) {
            bot.executeSend(chatId, "Error al realizar la búsqueda: " + e.getMessage());
            e.printStackTrace();
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
