package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;

import java.util.List;
import java.util.Map;

public class SolicitudesCommand implements Command {

    private final ApiClientService apiClient;

    public SolicitudesCommand(ApiClientService apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public String getCommand() {
        return "/solicitudes";
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
        if (args == null || args.isBlank()) {
            bot.executeSend(chatId, "Uso: /solicitudes <hechoId>");
            return;
        }
        String hechoId = args.trim();
        List<Map<String, Object>> sols = apiClient.listarSolicitudesPorHecho(hechoId);
        if (sols == null || sols.isEmpty()) {
            bot.executeSend(chatId, "No hay solicitudes para el hecho " + hechoId);
        } else {
            bot.sendListGeneric(chatId, sols, "Solicitudes para hecho " + hechoId + ":");
        }
    }

    @Override
    public String getHelp() {
        return "/solicitudes <hechoId> - Lista las solicitudes de borrado para un hecho específico.";
    }
}
