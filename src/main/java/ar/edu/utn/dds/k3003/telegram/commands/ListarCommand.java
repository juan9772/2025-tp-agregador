package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;
import java.util.List;
import java.util.Map;

public class ListarCommand implements Command {

    private final ApiClientService apiClient;

    public ListarCommand(ApiClientService apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public String getCommand() {
        return "/listar";
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
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
        return "/listar [coleccion] - Lista todos los hechos o los de una colección específica.";
    }
}
