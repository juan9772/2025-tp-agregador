package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;

import java.util.List;
import java.util.Map;

public class FuentesCommand implements Command {

    private final ApiClientService apiClient;

    public FuentesCommand(ApiClientService apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public String getCommand() {
        return "/fuentes";
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
        List<Map<String, Object>> fuentes = apiClient.listarFuentes();
        if (fuentes == null || fuentes.isEmpty()) {
            bot.executeSend(chatId, "No se encontraron fuentes.");
        } else {
            bot.sendListGeneric(chatId, fuentes, "Fuentes disponibles:");
        }
    }

    @Override
    public String getHelp() {
        return "/fuentes - Lista todas las fuentes de hechos disponibles.";
    }
}
