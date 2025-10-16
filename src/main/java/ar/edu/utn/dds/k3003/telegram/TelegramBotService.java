package ar.edu.utn.dds.k3003.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@SuppressWarnings("deprecation")
public class TelegramBotService extends TelegramLongPollingBot {

    private final ApiClientService apiClient;
    private final ConversationManager convManager;

    @Value("${telegram.bot.token:${TELEGRAM_BOT_TOKEN:}}")
    private String botToken;

    @Value("${telegram.bot.username:${TELEGRAM_BOT_USERNAME:MyBot}}")
    private String botUsername;

    public TelegramBotService(ApiClientService apiClient, ConversationManager convManager) {
        this.apiClient = apiClient;
        this.convManager = convManager;
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
            if (update.hasMessage() && update.getMessage().hasText()) {
                long chatId = update.getMessage().getChatId();
                String text = update.getMessage().getText().trim();
                handleMessage(chatId, text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(long chatId, String text) {
        try {
            if (text.startsWith("/listar")) {
                String[] parts = text.split("\\s+", 2);
                if (parts.length > 1 && !parts[1].isBlank()) {
                    String colec = parts[1].trim();
                    List<Map<String, Object>> hechos = apiClient.listarHechosPorColeccion(colec);
                    sendListHechos(chatId, hechos, "Hechos en colección: " + colec);
                } else {
                    List<Map<String, Object>> hechos = apiClient.listarHechos();
                    sendListHechos(chatId, hechos, "Todos los hechos:");
                }
                return;
            }

            if (text.startsWith("/ver")) {
                String[] p = text.split("\\s+", 2);
                if (p.length < 2) {
                    executeSend(chatId, "Uso: /ver <id>");
                    return;
                }
                Map<String, Object> hecho = apiClient.obtenerHecho(p[1].trim());
                if (hecho == null) {
                    executeSend(chatId, "No se encontró el hecho con id " + p[1].trim());
                    return;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("Título: ").append(Objects.toString(hecho.getOrDefault("titulo", "(sin título)"))).append("\n");
                sb.append("Colección: ").append(Objects.toString(hecho.getOrDefault("nombreColeccion", "(sin colección)"))).append("\n");
                sb.append("Descripción: ").append(Objects.toString(hecho.getOrDefault("descripcion", "(sin descripción)"))).append("\n");
                Object pdis = hecho.get("pdis");
                if (pdis != null) sb.append("PDIs: ").append(String.valueOf(pdis)).append("\n");
                Object imgs = hecho.get("imagenes");
                if (imgs != null) sb.append("Imágenes: ").append(String.valueOf(imgs)).append("\n");
                executeSend(chatId, sb.toString());
                return;
            }

            if (text.equalsIgnoreCase("/crear")) {
                convManager.startCreating(chatId);
                executeSend(chatId, "Vamos a crear un hecho. ¿Título?");
                return;
            }

            if (text.startsWith("/pdis")) {
                String[] parts = text.split("\\s+", 2);
                if (parts.length > 1 && !parts[1].isBlank()) {
                    String hechoId = parts[1].trim();
                    List<Map<String, Object>> pdis = apiClient.buscarPdisPorHecho(hechoId);
                    if (pdis == null || pdis.isEmpty()) executeSend(chatId, "No se encontraron PDIs para el hecho " + hechoId);
                    else sendListGeneric(chatId, pdis, "PDIs para hecho " + hechoId + ":");
                } else {
                    List<Map<String, Object>> pdis = apiClient.listarPdis();
                    sendListGeneric(chatId, pdis, "Todos los PDIs:");
                }
                return;
            }

            if (text.startsWith("/agregarpdi")) {
                String[] parts = text.split("\\s+", 2);
                if (parts.length < 2 || parts[1].isBlank()) {
                    executeSend(chatId, "Uso: /agregarpdi <hechoId>");
                    return;
                }
                String hechoId = parts[1].trim();
                convManager.startAgregarPdi(chatId, hechoId);
                executeSend(chatId, "Iniciando flujo de PdI para hecho " + hechoId + ". Enviá la URL del PdI:");
                return;
            }

            if (text.startsWith("/solicitudes")) {
                String[] parts = text.split("\\s+", 2);
                if (parts.length < 2 || parts[1].isBlank()) {
                    executeSend(chatId, "Uso: /solicitudes <hechoId>");
                    return;
                }
                String hechoId = parts[1].trim();
                List<Map<String, Object>> sols = apiClient.listarSolicitudesPorHecho(hechoId);
                if (sols == null || sols.isEmpty()) executeSend(chatId, "No hay solicitudes para el hecho " + hechoId);
                else sendListGeneric(chatId, sols, "Solicitudes para hecho " + hechoId + ":");
                return;
            }

            if (text.startsWith("/solicitarborrado")) {
                String[] parts = text.split("\\s+", 2);
                if (parts.length < 2 || parts[1].isBlank()) {
                    executeSend(chatId, "Uso: /solicitarborrado <hechoId>");
                    return;
                }
                String hechoId = parts[1].trim();
                convManager.startCrearSolicitud(chatId, hechoId);
                executeSend(chatId, "Iniciando solicitud de borrado para hecho " + hechoId + ". Escribí una descripción de la solicitud:");
                return;
            }

            if (text.startsWith("/cambiarestadosolicitud")) {
                String[] parts = text.split("\\s+", 3);
                if (parts.length < 3) {
                    executeSend(chatId, "Uso: /cambiarestadosolicitud <solicitudId> <estado>");
                    return;
                }
                String solicitudId = parts[1].trim();
                String estado = parts[2].trim();
                Map<String, Object> payload = Map.of("estado", estado);
                Map<String, Object> updated = apiClient.actualizarSolicitud(solicitudId, payload);
                if (updated == null) executeSend(chatId, "Error actualizando la solicitud.");
                else executeSend(chatId, "Solicitud actualizada: " + updated.getOrDefault("id", solicitudId));
                return;
            }

            if (text.startsWith("/cambiarestado")) {
                String[] parts = text.split("\\s+", 3);
                if (parts.length < 3) {
                    executeSend(chatId, "Uso: /cambiarestado <hechoId> <estado>");
                    return;
                }
                String hechoId = parts[1].trim();
                String estado = parts[2].trim();
                Map<String, Object> updated = apiClient.modificarEstado(hechoId, estado);
                if (updated == null) executeSend(chatId, "Error cambiando estado del hecho.");
                else executeSend(chatId, "Hecho actualizado: " + updated.getOrDefault("id", hechoId));
                return;
            }

            if (text.startsWith("/fuentes")) {
                List<Map<String, Object>> fuentes = apiClient.listarFuentes();
                if (fuentes == null || fuentes.isEmpty()) executeSend(chatId, "No se encontraron fuentes.");
                else sendListGeneric(chatId, fuentes, "Fuentes disponibles:");
                return;
            }

            // manejar flujo conversacional
            ConversationManager.State state = convManager.getState(chatId);
            if (state != null && state != ConversationManager.State.IDLE) {
                String resp = convManager.handle(chatId, text, apiClient);
                executeSend(chatId, resp);
                return;
            }

            // ayuda por defecto
            String help = "Comandos disponibles:\n/listar [coleccion]\n/ver <id>\n/crear\n/pdis [hechoId]\n/agregarpdi <hechoId>\n/solicitudes <hechoId>\n/solicitarborrado <hechoId>\n/cambiarestado <hechoId> <estado>\n/cambiarestadosolicitud <id> <estado>\n/fuentes";
            executeSend(chatId, help);

        } catch (Exception e) {
            executeSend(chatId, "Error interno: " + e.getMessage());
        }
    }

    private void sendListHechos(long chatId, List<Map<String, Object>> hechos, String title) {
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

    private void sendListGeneric(long chatId, List<Map<String, Object>> items, String title) {
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

    private void executeSend(long chatId, String text) {
        SendMessage sm = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
