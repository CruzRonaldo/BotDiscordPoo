package org.example.commands.impl;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.example.commands.Command;

/**
 * Comando de ejemplo: /ping
 * Responde con la latencia (ping) del bot hacia la API de Discord.
 * Úsalo como plantilla para crear tus propios comandos: crea una clase
 * dentro de commands.impl, implementa Command, y regístrala en Main.
 */
public class PingCommand implements Command {

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String getDescripcion() {
        return "Muestra la latencia actual del bot.";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), getDescripcion());
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long inicio = System.currentTimeMillis();

        event.reply("🏓 Calculando ping...").queue(respuesta ->
                respuesta.editOriginalFormat(
                        "🏓 Pong! Latencia: `%d ms`",
                        System.currentTimeMillis() - inicio
                ).queue()
        );
    }
}
