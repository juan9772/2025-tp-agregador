package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;
import java.util.Map;

public class CambiarEstadoCommand implements Command {

    private final ApiClientService apiClient;

    public CambiarEstadoCommand(ApiClientService apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public String getCommand() {
        return "/cambiarestado";
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
        String[] parts = args.split("\\s+", 2);
        if (parts.length < 2) {
            bot.executeSend(chatId, "Uso: /cambiarestado <hechoId> <estado>");
            return;
        }
        String hechoId = parts[0].trim();
        String estado = parts[1].trim();
        Map<String, Object> updated = apiClient.modificarEstado(hechoId, estado);
        if (updated == null) {
            bot.executeSend(chatId, "Error cambiando estado del hecho.");
        } else {
            bot.executeSend(chatId, "Hecho actualizado: " + updated.getOrDefault("id", hechoId));
        }
    }

    @Override
    public String getHelp() {
        return "/cambiarestado <hechoId> <estado> - Cambia el estado de un hecho.";
    }
}
