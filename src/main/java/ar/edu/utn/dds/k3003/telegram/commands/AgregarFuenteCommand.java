package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.ConversationManager;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;

public class AgregarFuenteCommand implements Command {

    private final ConversationManager conversationManager;

    public AgregarFuenteCommand(ConversationManager conversationManager) {
        this.conversationManager = conversationManager;
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
        conversationManager.startAgregarFuente(chatId);
        bot.executeSend(chatId, "Iniciando proceso para agregar una nueva fuente. Por favor, ingrese el nombre de la fuente:");
    }

    @Override
    public String getCommand() {
        return "/agregarfuente";
    }

    @Override
    public String getHelp() {
        return "Inicia el proceso para agregar una nueva fuente de datos.";
    }
}
