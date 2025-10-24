package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.ConversationManager;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;

public class SolicitarBorradoCommand implements Command {

    private final ConversationManager convManager;

    public SolicitarBorradoCommand(ConversationManager convManager) {
        this.convManager = convManager;
    }

    @Override
    public String getCommand() {
        return "/solicitarborrado";
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
        if (args == null || args.isBlank()) {
            bot.executeSend(chatId, "Uso: /solicitarborrado <hechoId>");
            return;
        }
        String hechoId = args.trim();
        convManager.startCrearSolicitud(chatId, hechoId);
        bot.executeSend(chatId, "Iniciando solicitud de borrado para hecho " + hechoId + ". Escribí una descripción de la solicitud:");
    }

    @Override
    public String getHelp() {
        return "/solicitarborrado <hechoId> - Inicia el proceso para solicitar el borrado de un hecho.";
    }
}
