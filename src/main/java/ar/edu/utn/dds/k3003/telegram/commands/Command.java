package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.TelegramBotService;

public interface Command {

    String getCommand();

    void execute(Long chatId, String args, TelegramBotService bot);

    String getHelp();
}
