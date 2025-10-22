package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.telegram.TelegramBotService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication(scanBasePackages = "ar.edu.utn.dds.k3003")
public class WebApp {
    public static void main(String[] args) {
        SpringApplication.run(WebApp.class, args);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBotService telegramBotService) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(telegramBotService);
        return api;
    }
}
