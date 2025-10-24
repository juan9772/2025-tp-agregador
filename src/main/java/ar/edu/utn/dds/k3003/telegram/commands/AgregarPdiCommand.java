package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.ConversationManager;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;

public class AgregarPdiCommand implements Command {

    private final ConversationManager convManager;

    public AgregarPdiCommand(ConversationManager convManager) {
        this.convManager = convManager;
    }

    @Override
    public String getCommand() {
        return "/agregarpdi";
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
        if (args == null || args.isBlank()) {
            bot.executeSend(chatId, "Uso: /agregarpdi <hechoId>");
            return;
        }
        String hechoId = args.trim();
        convManager.startAgregarPdi(chatId, hechoId);
        bot.executeSend(chatId, "Iniciando flujo de PdI para hecho " + hechoId + ". Enviá la URL del PdI:");
    }

    @Override
    public String getHelp() {
        return "/agregarpdi <hechoId> - Inicia el flujo para agregar un Punto de Interés a un hecho.";
    }
}
