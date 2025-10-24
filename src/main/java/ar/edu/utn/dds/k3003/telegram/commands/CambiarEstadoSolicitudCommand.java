package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;
import java.util.Map;

public class CambiarEstadoSolicitudCommand implements Command {

    private final ApiClientService apiClient;

    public CambiarEstadoSolicitudCommand(ApiClientService apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public String getCommand() {
        return "/cambiarestadosolicitud";
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
        String[] parts = args.split("\\s+", 2);
        if (parts.length < 2) {
            bot.executeSend(chatId, "Uso: /cambiarestadosolicitud <solicitudId> <estado>");
            return;
        }
        String solicitudId = parts[0].trim();
        String estado = parts[1].trim();
        Map<String, Object> payload = Map.of("estado", estado);
        Map<String, Object> updated = apiClient.actualizarSolicitud(solicitudId, payload);
        if (updated == null) {
            bot.executeSend(chatId, "Error actualizando la solicitud.");
        } else {
            bot.executeSend(chatId, "Solicitud actualizada: " + updated.getOrDefault("id", solicitudId));
        }
    }

    @Override
    public String getHelp() {
        return "/cambiarestadosolicitud <solicitudId> <estado> - Cambia el estado de una solicitud de borrado.";
    }
}
