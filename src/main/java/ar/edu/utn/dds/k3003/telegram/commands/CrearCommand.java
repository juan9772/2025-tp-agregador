package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.busqueda.services.IndexadorService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;

public class CrearCommand implements Command {

    private final ConversationManager convManager;
    private final IndexadorService indexadorService;

    public CrearCommand(ConversationManager convManager, IndexadorService indexadorService) {
        this.convManager = convManager;
        this.indexadorService = indexadorService;
    }

    @Override
    public String getCommand() {
        return "/crear";
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
        // Ahora, el conversation manager se encargará de llamar al indexador cuando el hecho se cree.
        convManager.startCreating(chatId, indexadorService);
        bot.executeSend(chatId, "Vamos a crear un hecho. ¿Título?");
    }

    @Override
    public String getHelp() {
        return "/crear - Inicia el proceso de creación de un nuevo hecho.";
    }
}
