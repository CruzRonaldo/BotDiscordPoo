package org.example.votes;

import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * Le permite a cada Votacion saber cómo reconstruir su propio embed "en
 * curso" (con el conteo actualizado), sin que Votacion tenga que saber
 * si es una votación de F1, F2, o cualquier otra futura.
 * <p>
 * Cada comando (AislarUsuarioCommand, VotacionF2Command, etc.) provee su
 * propia implementación al crear la Votacion.
 */
@FunctionalInterface
public interface EmbedFactory {
    MessageEmbed crear(long votosSi, long votosNo);
}