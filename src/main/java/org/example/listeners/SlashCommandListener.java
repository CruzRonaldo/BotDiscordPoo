package org.example.listeners;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.example.commands.CommandManager;

/**
 * Escucha los eventos de JDA relacionados a slash commands y delega la
 * ejecución al CommandManager.
 *
 * Extiende ListenerAdapter para no tener que implementar todos los métodos
 * de la interfaz EventListener de JDA; solo sobrescribimos el que nos interesa.
 */
public class SlashCommandListener extends ListenerAdapter {

    private final CommandManager commandManager;

    public SlashCommandListener(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        commandManager.despachar(event);
    }
}
