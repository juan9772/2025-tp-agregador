package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;

import java.util.List;
import java.util.Map;

public class ColeccionesCommand implements Command {

    private final ApiClientService apiClient;
    private final ConversationManager conversationManager;

    public ColeccionesCommand(ApiClientService apiClient, ConversationManager conversationManager) {
        this.apiClient = apiClient;
        this.conversationManager = conversationManager;
    }

    @Override
    public String getCommand() {
        return "/colecciones";
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
        ConversationManager.Context context = conversationManager.getContext(chatId);
        String fuenteUrl = (String) context.payload.get("selected_fuente_url");

        if (fuenteUrl == null) {
            bot.executeSend(chatId, "No ha seleccionado una fuente. Use /usarfuente <nombre_de_la_fuente> para seleccionar una.");
            return;
        }

        apiClient.setFuenteActiva(fuenteUrl);

        List<Map<String, Object>> colecciones = apiClient.listarColecciones();
        if (colecciones == null || colecciones.isEmpty()) {
            bot.executeSend(chatId, "No se encontraron colecciones.");
            return;
        }

        StringBuilder sb = new StringBuilder("Colecciones disponibles:\n");
        for (Map<String, Object> col : colecciones) {
            sb.append("- ").append(col.getOrDefault("nombre", "(sin nombre)"))
              .append(": ")
              .append(col.getOrDefault("descripcion", "(sin descripción)"))
              .append("\n");
        }
        bot.executeSend(chatId, sb.toString());
    }

    @Override
    public String getHelp() {
        return "/colecciones - Lista todas las colecciones de hechos disponibles en la fuente seleccionada.";
    }
}
