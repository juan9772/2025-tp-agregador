package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UsarFuenteCommand implements Command {

    private final ApiClientService apiClientService;
    private final ConversationManager conversationManager;

    public UsarFuenteCommand(ApiClientService apiClientService, ConversationManager conversationManager) {
        this.apiClientService = apiClientService;
        this.conversationManager = conversationManager;
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
        if (args == null || args.isEmpty()) {
            bot.executeSend(chatId, "Uso: /usarfuente <nombre_de_la_fuente>");
            return;
        }

        try {
            List<Map<String, Object>> fuentes = apiClientService.listarFuentes();
            Optional<Map<String, Object>> fuenteOptional = fuentes.stream()
                    .filter(f -> args.equalsIgnoreCase(String.valueOf(f.get("nombre"))))
                    .findFirst();

            if (fuenteOptional.isPresent()) {
                Map<String, Object> fuente = fuenteOptional.get();
                String endpoint = (String) fuente.get("endpoint");
                String id = String.valueOf(fuente.get("id"));
                String nombre = (String) fuente.get("nombre");

                ConversationManager.Context context = conversationManager.getContext(chatId);
                context.payload.put("selected_fuente_url", endpoint);
                context.payload.put("selected_fuente_id", id);
                context.payload.put("selected_fuente_nombre", nombre);

                bot.executeSend(chatId, "Fuente cambiada a: " + nombre);
            } else {
                bot.executeSend(chatId, "No se encontró la fuente: " + args);
            }
        } catch (Exception e) {
            bot.executeSend(chatId, "Error al cambiar de fuente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getCommand() {
        return "/usarfuente";
    }

    @Override
    public String getHelp() {
        return "Uso: /usarfuente <nombre_de_la_fuente> - Cambia la fuente de datos activa para las consultas.";
    }
}
