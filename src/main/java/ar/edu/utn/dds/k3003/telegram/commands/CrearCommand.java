package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.ConversationManager;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;

public class CrearCommand implements Command {

    private final ConversationManager convManager;

    public CrearCommand(ConversationManager convManager) {
        this.convManager = convManager;
    }

    @Override
    public String getCommand() {
        return "/crear";
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
        convManager.startCreating(chatId);
        bot.executeSend(chatId, "Vamos a crear un hecho. ¿Título?");
    }

    @Override
    public String getHelp() {
        return "/crear - Inicia el proceso de creación de un nuevo hecho.";
    }
}
