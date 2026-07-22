package org.example.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * Contrato que debe cumplir todo comando slash del bot.
 * <p>
 * Cada comando concreto (PingCommand, InfoCommand, etc.) implementa esta
 * interfaz. Esto permite que el {@link CommandManager} trate a todos los
 * comandos de forma uniforme (polimorfismo), sin importar su lógica interna.
 */
public interface Command {

    /**
     * @return el nombre del comando tal cual se escribirá en Discord (ej: "ping").
     * Debe estar en minúsculas y sin espacios.
     */
    String getName();

    String getDescripcion();

    /**
     * @return la definición (nombre, descripción, opciones) que JDA necesita
     * para registrar el comando como slash command ante Discord.
     */
    SlashCommandData getCommandData();

    /**
     * Lógica que se ejecuta cuando un usuario invoca este comando.
     *
     * @param event el evento de interacción que provee JDA, con toda la
     *              información de quién ejecutó el comando, en qué canal, etc.
     */
    void execute(SlashCommandInteractionEvent event);
}
