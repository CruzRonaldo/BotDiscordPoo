package org.example.commands.impl;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.example.commands.Command;
import org.example.votes.RolTemporalManager;
import org.example.votes.Votacion;
import org.example.votes.VotacionManager;

import java.awt.Color;

/**
 * Comando /f2 usuario:@alguien
 * Inicia una votación para QUITAR antes de tiempo el rol de F1 que un
 * usuario ya tiene; depende de que ese usuario haya ganado /f1 previamente.
 */
public class DesaislarUsuarioCommand implements Command {

    private static final long DURACION_VOTACION_SEGUNDOS = 15;

    private final VotacionManager votacionManager;
    private final RolTemporalManager rolTemporalManager;
    private final String roleId;

    public DesaislarUsuarioCommand(VotacionManager votacionManager, RolTemporalManager rolTemporalManager,
                                   String roleId) {
        this.votacionManager = votacionManager;
        this.rolTemporalManager = rolTemporalManager;
        this.roleId = roleId;
    }

    @Override
    public String getName() {
        return "f2";
    }

    @Override
    public String getDescripcion() {
        return "Inicia una votación para quitarle aislamiento a un usuario antes de tiempo.";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), getDescripcion())
                .addOption(OptionType.USER, "usuario", "Usuario al que se le votará quitar el aislamiento", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) {
            event.reply("⚠️ Este comando solo se puede usar dentro de un servidor.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Role rol = event.getGuild().getRoleById(roleId);
        if (rol == null) {
            event.reply("⚠️ No se encontró el rol de F1 configurado. Revisa config.properties.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        User targetUser = event.getOption("usuario").getAsUser();

        // Consultamos directamente a Discord si el usuario tiene el rol
        // ahora mismo, en vez de fiarnos de la memoria interna del bot
        // (así funciona aunque el bot se haya reiniciado o el rol se haya
        // asignado manualmente).
        event.getGuild().retrieveMember(targetUser).queue(member -> {
            if (!member.getRoles().contains(rol)) {
                event.reply("⚠️ " + targetUser.getAsMention() + " no tiene el rol de F1 en este momento.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            iniciarVotacion(event, targetUser);
        });
    }

    private void iniciarVotacion(SlashCommandInteractionEvent event, User targetUser) {
        if (!votacionManager.hayEspacioParaNuevaVotacion()) {
            event.reply("⚠️ Ya hay 4 votaciones activas en este momento. Espera a que alguna termine.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String iniciador = event.getUser().getAsMention();
        String targetMencion = targetUser.getAsMention();

        event.replyEmbeds(construirEmbedEnCurso(iniciador, targetMencion, 0, 0))
                .addActionRow(
                        Button.success("voto_si", "✅ Sí"),
                        Button.danger("voto_no", "❌ No")
                )
                .queue(hook -> hook.retrieveOriginal().queue(mensaje -> {
                    Votacion votacion = new Votacion(
                            mensaje.getId(), iniciador, targetUser.getId(), targetMencion,
                            (si, no) -> construirEmbedEnCurso(iniciador, targetMencion, si, no)
                    );

                    votacionManager.iniciarVotacion(votacion, DURACION_VOTACION_SEGUNDOS, votacionFinalizada -> {
                        long si = votacionFinalizada.contar(Votacion.Opcion.SI);
                        long no = votacionFinalizada.contar(Votacion.Opcion.NO);
                        boolean aprobada = si > no;

                        mensaje.editMessageEmbeds(construirEmbedFinal(iniciador, targetMencion, si, no, aprobada))
                                .setComponents() // quita los botones al terminar
                                .queue();

                        if (aprobada) {
                            quitarRolAlGanador(event, targetUser);
                        }
                    });
                }));
    }

    private void quitarRolAlGanador(SlashCommandInteractionEvent event, User targetUser) {
        if (event.getGuild() == null) {
            return;
        }

        Role rol = event.getGuild().getRoleById(roleId);
        if (rol == null) {
            System.err.println("❌ No se encontró el rol con ID " + roleId + ". Revisa config.properties.");
            return;
        }

        event.getGuild().retrieveMember(targetUser).queue(member ->
                rolTemporalManager.quitarRolAntesDeTiempo(event.getGuild(), member, rol)
        );
    }

    private MessageEmbed construirEmbedEnCurso(String iniciador, String targetMencion, long si, long no) {
        String descripcion = "Votación iniciada por " + iniciador + "\n\n"
                + "**¿Quitarle F1 a " + targetMencion + " antes de tiempo?**\n"
                + "------------------------------\n"
                + "Pulsa F1 para votar SÍ\n"
                + "Pulsa F2 para votar NO\n"
                + "------------------------------\n"
                + "Duración de la votación: " + DURACION_VOTACION_SEGUNDOS + " segundos.\n"
                + "Recuento actual de votos:";
        return new EmbedBuilder()
                .setDescription(descripcion)
                .addField("✅ Sí", String.valueOf(si), true)
                .addField("❌ No", String.valueOf(no), true)
                .setColor(Color.ORANGE)
                .build();
    }

    private MessageEmbed construirEmbedFinal(String iniciador, String targetMencion, long si, long no, boolean aprobada) {
        return new EmbedBuilder()
                .setTitle(aprobada ? "✅ VOTO ADMITIDO." : "❌ VOTO RECHAZADO.")
                .setDescription(aprobada
                        ? "Desaislando a " + targetMencion + " antes de tiempo."
                        : "Los votos positivos deben superar a los votos negativos.")
                .addField("✅ Sí", String.valueOf(si), true)
                .addField("❌ No", String.valueOf(no), true)
                .setColor(aprobada ? Color.GREEN : Color.RED)
                .build();
    }
}
