package ar.edu.utn.dds.k3003.telegram.commands;

import ar.edu.utn.dds.k3003.busqueda.services.IndexadorService;
import ar.edu.utn.dds.k3003.telegram.ApiClientService;
import ar.edu.utn.dds.k3003.telegram.ConversationManager;
import ar.edu.utn.dds.k3003.telegram.MetricasService;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CommandFactory {

    private final Map<String, Command> commandMap;

    public CommandFactory(ApiClientService apiClient, ConversationManager convManager, IndexadorService indexadorService, MetricasService metricasService) {
        // 1. Create all the standard commands
        List<Command> commands = Stream.of(
                // Comandos de consulta
                new ListarCommand(apiClient, convManager),
                new VerCommand(apiClient),
                new PdisCommand(apiClient),
                new SolicitudesCommand(apiClient),
                new FuentesCommand(apiClient),
                new ColeccionesCommand(apiClient, convManager),
                new BuscarCommand(apiClient, convManager, metricasService),
                new SiguienteCommand(apiClient, convManager),
                new UsarFuenteCommand(apiClient, convManager),

                // Comandos que inician conversación
                new CrearCommand(convManager, indexadorService), // <-- CON INDEXADOR
                new AgregarPdiCommand(convManager, indexadorService), // <-- CON INDEXADOR
                new SolicitarBorradoCommand(convManager),
                new AgregarFuenteCommand(convManager),

                // Comandos de acción
                new CambiarEstadoSolicitudCommand(apiClient),
                new AprobarBorradoCommand(apiClient, indexadorService), // <-- CON INDEXADOR
                new RechazarBorradoCommand(apiClient)

        ).collect(Collectors.toList());

        // 2. Create the command map from the standard commands
        this.commandMap = commands.stream().collect(Collectors.toMap(Command::getCommand, Function.identity()));

        // 3. Create and add the special HelpCommand
        Command helpCommand = new HelpCommand(this.commandMap.values());
        this.commandMap.put(helpCommand.getCommand(), helpCommand);
    }

    public Command getCommand(String command) {
        return commandMap.get(command);
    }

    public Collection<Command> getCommands() {
        return commandMap.values();
    }
}
