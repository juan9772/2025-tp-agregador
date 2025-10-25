package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;

import java.util.Map;

public class AprobarBorradoCommand implements Command {

    private final ApiClientService apiClientService;

    public AprobarBorradoCommand(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
        if (args == null || args.isEmpty()) {
            bot.executeSend(chatId, "Uso: /aprobarborrado <solicitudId>");
            return;
        }
        String solicitudId = args.trim();
        try {
            Map<String, Object> payload = Map.of(
                    "descripcion", "",
                    "estado", "ACEPTADA"
            );
            Map<String, Object> result = apiClientService.actualizarSolicitud(solicitudId, payload);
            bot.executeSend(chatId, "Solicitud " + solicitudId + " aprobada con éxito. Nuevo estado: " + result.get("estado"));
        } catch (Exception e) {
            bot.executeSend(chatId, "Error al aprobar la solicitud " + solicitudId + ": " + e.getMessage());
        }
    }

    @Override
    public String getCommand() {
        return "/aprobarborrado";
    }

    @Override
    public String getHelp() {
        return "Uso: /aprobarborrado <solicitudId> - Aprueba una solicitud de borrado de un PDI.";
    }
}
