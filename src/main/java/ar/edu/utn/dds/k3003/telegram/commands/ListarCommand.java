package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;
import java.util.List;
import java.util.Map;

public class ListarCommand implements Command {

    private final ApiClientService apiClient;
    private final ConversationManager conversationManager;

    public ListarCommand(ApiClientService apiClient, ConversationManager conversationManager) {
        this.apiClient = apiClient;
        this.conversationManager = conversationManager;
    }

    @Override
    public String getCommand() {
        return "/listar";
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

        if (args != null && !args.isBlank()) {
            String colec = args.trim();
            List<Map<String, Object>> hechos = apiClient.listarHechosPorColeccion(colec);
            bot.sendListHechos(chatId, hechos, "Hechos en colección: " + colec);
        } else {
            List<Map<String, Object>> hechos = apiClient.listarHechos();
            bot.sendListHechos(chatId, hechos, "Todos los hechos:");
        }
    }

    @Override
    public String getHelp() {
        return "/listar [coleccion] - Lista todos los hechos o los de una colección específica en la fuente seleccionada.";
    }
}
