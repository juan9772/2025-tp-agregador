package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;

import java.util.List;
import java.util.Map;

public class PdisCommand implements Command {

    private final ApiClientService apiClient;

    public PdisCommand(ApiClientService apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public String getCommand() {
        return "/pdis";
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
        if (args != null && !args.isBlank()) {
            String hechoId = args.trim();
            List<Map<String, Object>> pdis = apiClient.buscarPdisPorHecho(hechoId);
            if (pdis == null || pdis.isEmpty()) {
                bot.executeSend(chatId, "No se encontraron PDIs para el hecho " + hechoId);
            } else {
                bot.sendListGeneric(chatId, pdis, "PDIs para hecho " + hechoId + ":");
            }
        } else {
            List<Map<String, Object>> pdis = apiClient.listarPdis();
            bot.sendListGeneric(chatId, pdis, "Todos los PDIs:");
        }
    }

    @Override
    public String getHelp() {
        return "/pdis [hechoId] - Lista todos los PDIs o los de un hecho específico.";
    }
}
