package org.example.commands.impl;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.example.commands.Command;

import java.awt.*;

public class ModSmashCommand implements Command {

    private final String urlDescarga;

    public ModSmashCommand(String urlDescarga) {
        this.urlDescarga = urlDescarga;
    }

    @Override
    public String getName() {
        return "mods";
    }

    @Override
    public String getDescripcion() {
        return "Obtén el enlace de descarga de los mods.";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), getDescripcion());
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Mods")
                .setDescription("[Abrir enlace](" + urlDescarga + ")")
                .setColor(Color.CYAN)
                .build();

        event.replyEmbeds(embed)
                .addActionRow(Button.link(urlDescarga,"Abrir"))
                .queue();
    }
}
