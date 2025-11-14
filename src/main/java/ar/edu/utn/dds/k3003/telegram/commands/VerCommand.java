package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.TelegramBotService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class VerCommand implements Command {

    private final ApiClientService apiClient;

    public VerCommand(ApiClientService apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public String getCommand() {
        return "/ver";
    }

    @Override
    public void execute(Long chatId, String args, TelegramBotService bot) {
        if (args == null || args.isBlank()) {
            bot.executeSend(chatId, "Uso: /ver <id>");
            return;
        }
        String hechoId = args.trim();
        Map<String, Object> hecho = apiClient.obtenerHecho(hechoId);
        if (hecho == null) {
            bot.executeSend(chatId, "No se encontró el hecho con id " + hechoId);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Título: ").append(Objects.toString(hecho.getOrDefault("titulo", "(sin título)"))).append("\n");
        sb.append("Colección: ").append(Objects.toString(hecho.getOrDefault("nombreColeccion", "(sin colección)"))).append("\n");
        sb.append("Descripción: ").append(Objects.toString(hecho.getOrDefault("descripcion", "(sin descripción)"))).append("\n");

        Object imgs = hecho.get("imagenes");
        if (imgs != null) sb.append("Imágenes: ").append(String.valueOf(imgs)).append("\n");

        // First attempt to get PDIs
        List<Map<String, Object>> pdis = apiClient.buscarPdisPorHecho(hechoId);

        // If the first attempt fails, wait a bit and retry.
        // This helps mitigate race conditions where the PDI service hasn't finished processing yet.
        if (pdis == null || pdis.isEmpty()) {
            try {
                TimeUnit.SECONDS.sleep(2); // Wait for 2 seconds
                pdis = apiClient.buscarPdisPorHecho(hechoId); // Retry
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                bot.executeSend(chatId, "Se interrumpió la espera de datos del PDI.");
            }
        }

        if (pdis != null && !pdis.isEmpty()) {
            sb.append("PDIs: \n");
            for (Map<String, Object> pdi : pdis) {
                sb.append(" - ID: ").append(pdi.getOrDefault("id", "(no id)")).append("\n");
                if (pdi.containsKey("imagen_url")) sb.append("   Imagen: ").append(pdi.get("imagen_url")).append("\n");
                if (pdi.containsKey("descripcion")) sb.append("   Descripcion: ").append(truncate(Objects.toString(pdi.get("descripcion")), 200)).append("\n");
                if (pdi.containsKey("contenido")) sb.append("   Contenido: ").append(truncate(Objects.toString(pdi.get("contenido")), 200)).append("\n");
                if (pdi.containsKey("ocr_texto")) sb.append("   OCR: ").append(truncate(Objects.toString(pdi.get("ocr_texto")), 200)).append("\n");
                if (pdi.containsKey("lugar")) sb.append("   Origen: ").append(pdi.get("lugar")).append("\n");

                Object momentoObj = pdi.get("momento");
                if (momentoObj instanceof List) {
                    List<Number> momentoLista = (List<Number>) momentoObj;
                    if (momentoLista.size() >= 3) {
                        String fechaFormateada = String.format("%02d/%02d/%d", momentoLista.get(2).intValue(), momentoLista.get(1).intValue(), momentoLista.get(0).intValue());
                        sb.append("   Fecha de Carga: ").append(fechaFormateada).append("\n");
                    }
                }

                Object etiquetas = pdi.get("etiquetas_auto");
                if (etiquetas instanceof List) {
                    List<Object> et = (List<Object>) etiquetas;
                    String joined = et.stream().map(Objects::toString).collect(Collectors.joining(", "));
                    sb.append("   Etiquetas: ").append(joined).append("\n");
                } else if (etiquetas != null) {
                    sb.append("   Etiquetas: ").append(etiquetas.toString()).append("\n");
                }
            }
        } else {
            Object pdisIn = hecho.get("pdis");
            if (pdisIn != null) sb.append("PDIs (desde hecho): ").append(String.valueOf(pdisIn)).append("\n");
        }

        bot.executeSend(chatId, sb.toString());
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    @Override
    public String getHelp() {
        return "/ver <id> - Muestra los detalles de un hecho específico.";
    }
}
