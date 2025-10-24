package ar.edu.utn.dds.k3003.telegram;

import ar.edu.utn.dds.k3003.telegram.commands.Command;
import ar.edu.utn.dds.k3003.telegram.commands.CommandFactory;
import ar.edu.utn.dds.k3003.telegram.states.ConversationState;
import ar.edu.utn.dds.k3003.telegram.states.IdleState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@SuppressWarnings("deprecation")
public class TelegramBotService extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotService.class);

    private final ConversationManager convManager;
    private final CommandFactory commandFactory;
    private final ApiClientService apiClient;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    public TelegramBotService(ConversationManager convManager, CommandFactory commandFactory, ApiClientService apiClient) {
        this.convManager = convManager;
        this.commandFactory = commandFactory;
        this.apiClient = apiClient;
    }

    @Override
    public String getBotUsername() {
        return botUsername == null || botUsername.isBlank() ? "MyBot" : botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update == null) return;
        try {
            log.debug("Received update: {}", update);
            if (update.hasMessage() && update.getMessage().hasText()) {
                long chatId = update.getMessage().getChatId();
                String text = update.getMessage().getText().trim();
                log.info("Message from chatId {}: {}", chatId, text);
                handleMessage(chatId, text);
            } else {
                log.debug("Update has no text message: {}", update);
            }
        } catch (Exception e) {
            log.error("Error processing update", e);
            e.printStackTrace();
        }
    }

    private void handleMessage(long chatId, String text) {
        try {
            log.debug("Handling message for chat {}: {}", chatId, text);

            // 1. Check for a known command
            if (text.startsWith("/")) {
                String[] parts = text.split("\\s+", 2);
                String commandStr = parts[0];
                Command command = commandFactory.getCommand(commandStr);

                if (command != null) {
                    String args = (parts.length > 1) ? parts[1] : "";
                    command.execute(chatId, args, this);
                    return; // Command executed, we are done.
                }
            }

            // 2. If not a known command, check for an active conversation
            ConversationState state = convManager.getState(chatId);
            if (!(state instanceof IdleState)) {
                String resp = convManager.handle(chatId, text, apiClient);
                executeSend(chatId, resp);
                return; // Conversation handled, we are done.
            }

            // 3. Default behavior: not a known command and no active conversation -> Show help
            Command helpCommand = commandFactory.getCommand("/help");
            if (helpCommand != null) {
                helpCommand.execute(chatId, "", this);
            } else {
                // Fallback in case help command is not configured
                executeSend(chatId, "Comando no reconocido.");
            }

        } catch (Exception e) {
            log.error("Error interno manejando mensaje", e);
            executeSend(chatId, "Error interno: " + e.getMessage());
        }
    }

    public void sendListHechos(long chatId, List<Map<String, Object>> hechos, String title) {
        if (hechos == null || hechos.isEmpty()) {
            executeSend(chatId, "No hay hechos.");
            return;
        }
        StringBuilder sb = new StringBuilder(title + "\n");
        for (Map<String, Object> h : hechos) {
            sb.append(h.getOrDefault("id", "(no id)"))
                    .append(" - ")
                    .append(h.getOrDefault("titulo", "(sin título)"))
                    .append(" (colección: ")
                    .append(h.getOrDefault("nombreColeccion", "(sin colección)"))
                    .append(")\n");
        }
        executeSend(chatId, sb.toString());
    }

    public void sendListGeneric(long chatId, List<Map<String, Object>> items, String title) {
        if (items == null || items.isEmpty()) {
            executeSend(chatId, "No hay elementos.");
            return;
        }
        StringBuilder sb = new StringBuilder(title + "\n");
        for (Map<String, Object> it : items) {
            sb.append(it.getOrDefault("id", "(no id)"))
                    .append(" - ")
                    .append(it.getOrDefault("titulo", it.getOrDefault("nombre", it.getOrDefault("descripcion", "(sin descripción)"))))
                    .append("\n");
        }
        executeSend(chatId, sb.toString());
    }

    public void executeSend(long chatId, String text) {
        SendMessage sm = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build();
        try {
            execute(sm);
            log.debug("Sent message to {} ({} chars)", chatId, text == null ? 0 : text.length());
        } catch (TelegramApiException e) {
            log.error("Error sending message to {}", chatId, e);
            e.printStackTrace();
        }
    }
}
