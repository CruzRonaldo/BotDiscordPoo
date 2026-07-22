package org.example.commands.impl;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.example.commands.Command;
import org.example.commands.CommandManager;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;

/**
 * Comando /help: muestra la lista de comandos disponibles con su
 * descripción, leyéndolos directamente de CommandManager.
 * <p>
 * No hay que tocar esta clase cada vez que se agrega un comando nuevo:
 * en cuanto se registra en DiscordBot (registrarComando(new TuComando())),
 * aparece aquí automáticamente, porque ambos leen de la misma fuente.
 */
public class HelpCommand implements Command {

    private final CommandManager commandManager;

    public HelpCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescripcion() {
        return "Muestra la lista de comandos disponibles.";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), getDescripcion());
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📖 Comandos disponibles")
                .setColor(Color.CYAN);

        List<Command> comandosOrdenados = commandManager.obtenerComandos().stream()
                .sorted(Comparator.comparing(Command::getName))
                .toList();

        for (Command comando : comandosOrdenados) {
            embed.addField(
                    "/" + comando.getName(),
                    comando.getDescripcion(),
                    false
            );
        }

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }
}