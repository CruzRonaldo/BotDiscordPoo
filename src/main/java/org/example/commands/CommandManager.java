package org.example.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Administra todos los comandos del bot: los mantiene registrados en memoria,
 * los sube a Discord (updateCommands) y despacha cada interacción al comando
 * correspondiente cuando un usuario lo ejecuta.
 * Aplica el principio de responsabilidad única (SRP): esta clase solo se
 * encarga de "administrar comandos", no de la lógica de cada uno.
 */
public class CommandManager {

    private final Map<String, Command> comandos = new LinkedHashMap<>();

    /**
     * Registra un nuevo comando en el administrador.
     *
     * @param command instancia de un comando concreto (implementa Command).
     */
    public void registrar(Command command) {
        comandos.put(command.getName(), command);
    }

    /*
     *  @return todos los comandos registrados actualmente. Lo usa /help para
     * construir su lista automáticamente, sin tener que mantenerla a mano.
     */
    public Collection<Command> obtenerComandos() {
        return comandos.values();
    }

    /**
     * Sube a Discord (de forma global) la definición de todos los comandos
     * registrados. Debe llamarse una vez que el JDA esté listo.
     * Nota: los comandos globales pueden tardar hasta 1 hora en propagarse
     * la primera vez; para pruebas rápidas conviene registrar por guild.
     */

    public void registrarComandosEnDiscord(JDA jda) {
        var commandDataList = comandos.values()
                .stream()
                .map(Command::getCommandData)
                .toList();

        jda.updateCommands()
                .addCommands(commandDataList)
                .queue();
    }

    /**
     * Busca y ejecuta el comando correspondiente al evento recibido.
     * Si el comando no existe (por ejemplo, quedó registrado en Discord,
     * pero fue eliminado del código), responde con un mensaje de error.
     */
    public void despachar(SlashCommandInteractionEvent event) {
        Command comando = comandos.get(event.getName());

        if (comando == null) {
            event.reply("⚠️ Comando no reconocido.").setEphemeral(true).queue();
            return;
        }

        comando.execute(event);
    }
}
