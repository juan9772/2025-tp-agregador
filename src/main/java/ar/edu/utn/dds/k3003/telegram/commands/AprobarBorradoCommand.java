package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.busqueda.services.IndexadorService;
import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;

import java.util.Map;

public class AprobarBorradoCommand implements Command {

    private final ApiClientService apiClientService;
    private final IndexadorService indexadorService;

    public AprobarBorradoCommand(ApiClientService apiClientService, IndexadorService indexadorService) {
        this.apiClientService = apiClientService;
        this.indexadorService = indexadorService;
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
        if (args == null || args.isEmpty()) {
            bot.executeSend(chatId, "Uso: /aprobarborrado <solicitudId>");
            return;
        }
        String solicitudId = args.trim();
        try {
            // Primero, obtenemos la solicitud para saber a qué hecho se refiere.
            Map<String, Object> solicitud = apiClientService.obtenerSolicitud(solicitudId);
            String hechoId = (String) solicitud.get("hechoId");

            // Preparamos y enviamos la actualización del estado de la solicitud.
            Map<String, Object> payload = Map.of("estado", "ACEPTADA");
            apiClientService.actualizarSolicitud(solicitudId, payload);

            // Si todo fue bien, borramos el hecho del índice de búsqueda.
            if (hechoId != null && !hechoId.isEmpty()) {
                indexadorService.borrar(hechoId);
                bot.executeSend(chatId, "Solicitud " + solicitudId + " aprobada. El hecho " + hechoId + " ha sido eliminado del índice de búsqueda.");
            } else {
                bot.executeSend(chatId, "Solicitud " + solicitudId + " aprobada, pero no se pudo determinar el hecho asociado para eliminarlo del índice.");
            }

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
        return "Uso: /aprobarborrado <solicitudId> - Aprueba una solicitud de borrado, eliminando el hecho del índice de búsqueda.";
    }
}
