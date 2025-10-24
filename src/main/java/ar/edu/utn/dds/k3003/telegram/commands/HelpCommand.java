package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.TelegramBotService;

import java.util.Collection;

public class HelpCommand implements Command {

    private final Collection<Command> allCommands;

    public HelpCommand(Collection<Command> allCommands) {
        this.allCommands = allCommands;
    }

    @Override
    public String getCommand() {
        return "/help";
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
        StringBuilder sb = new StringBuilder("Comandos disponibles:\n");
        for (Command cmd : allCommands) {
            sb.append(cmd.getHelp()).append("\n");
        }
        // Add its own help
        sb.append(this.getHelp()).append("\n");
        bot.executeSend(chatId, sb.toString());
    }

    @Override
    public String getHelp() {
        return "/help - Muestra esta ayuda.";
    }
}
