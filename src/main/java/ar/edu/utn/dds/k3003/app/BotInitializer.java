package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.telegram.TelegramBotService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class BotInitializer {

    private final TelegramBotService telegramBotService;

    public BotInitializer(TelegramBotService telegramBotService) {
        this.telegramBotService = telegramBotService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBotService);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
