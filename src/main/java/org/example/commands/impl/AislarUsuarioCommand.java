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
 * Comando /f1 usuario:@alguien
 * Inicia una votación estilo L4D2 (botones Sí/No) para decidir si se le
 * da el rol de "F1" a un usuario específico durante 2 horas.
 */
public class AislarUsuarioCommand implements Command {

    private static final long DURACION_VOTACION_SEGUNDOS = 15;
    private static final long DURACION_ROL_SEGUNDOS = 60 * 60 * 2; // 2 horas

    private final VotacionManager votacionManager;
    private final RolTemporalManager rolTemporalManager;
    private final String roleId;

    public AislarUsuarioCommand(VotacionManager votacionManager, RolTemporalManager rolTemporalManager, String roleId) {
        this.votacionManager = votacionManager;
        this.rolTemporalManager = rolTemporalManager;
        this.roleId = roleId;
    }

    @Override
    public String getName() {
        return "f1";
    }

    @Override
    public String getDescripcion() {
        return "Inicia una votación para darle F1 a un usuario por 2 horas.";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Inicia una votación para darle F1 a un usuario por 2 horas.")
                .addOption(OptionType.USER, "usuario", "Usuario al que se le dará F1", true);
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

        // Consultamos a Discord si el usuario ya tiene el rol antes de
        // arrancar la votación; no tiene sentido volver a votar si ya lo tiene.
        event.getGuild().retrieveMember(targetUser).queue(member -> {
            if (member.getRoles().contains(rol)) {
                event.reply("⚠️ " + targetUser.getAsMention() + " ya esta aislado en este momento.")
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
                            asignarRol(event, targetUser);
                        }
                    });
                }));
    }

    private void asignarRol(SlashCommandInteractionEvent event, User targetUser) {
        if (event.getGuild() == null) {
            return; // por seguridad; /f1 solo tiene sentido dentro de un servidor
        }

        Role rol = event.getGuild().getRoleById(roleId);
        if (rol == null) {
            System.err.println("❌ No se encontró el rol con ID " + roleId + ". Revisa config.properties.");
            return;
        }

        event.getGuild().retrieveMember(targetUser).queue(member ->
                rolTemporalManager.asignarRolTemporal(event.getGuild(), member, rol, DURACION_ROL_SEGUNDOS)
        );
    }

    private MessageEmbed construirEmbedEnCurso(String iniciador, String targetMencion, long si, long no) {
        String descripcion = "Votación iniciada por " + iniciador + "\n\n"
                + "**¿Aislar a jugador " + targetMencion + "?**\n"
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
                        ? "Aislando a :" + targetMencion + " por 2 horas."
                        : "Los votos positivos deben superar a los votos negativos.")
                .addField("✅ Sí", String.valueOf(si), true)
                .addField("❌ No", String.valueOf(no), true)
                .setColor(aprobada ? Color.GREEN : Color.RED)
                .build();
    }
}