package org.example.listeners;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.example.votes.Votacion;
import org.example.votes.VotacionManager;

/**
 * Escucha los clics en los botones "voto_si" / "voto_no" y actualiza el
 * conteo de la votación correspondiente.
 *
 * No sabe (ni le importa) si la votación es de /f1, /f2, o cualquier otra
 * futura: cada Votacion sabe reconstruir su propio embed.
 */
public class VotoButtonListener extends ListenerAdapter {

    private static final String BOTON_SI = "voto_si";
    private static final String BOTON_NO = "voto_no";

    private final VotacionManager votacionManager;

    public VotoButtonListener(VotacionManager votacionManager) {
        this.votacionManager = votacionManager;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String botonId = event.getComponentId();

        if (!botonId.equals(BOTON_SI) && !botonId.equals(BOTON_NO)) {
            return; // no es un botón de votación, lo ignoramos
        }

        Votacion votacion = votacionManager.obtener(event.getMessageId());

        if (votacion == null) {
            event.reply("⚠️ Esta votación ya terminó.").setEphemeral(true).queue();
            return;
        }

        Votacion.Opcion opcion = botonId.equals(BOTON_SI) ? Votacion.Opcion.SI : Votacion.Opcion.NO;
        votacion.votar(event.getUser().getId(), opcion);

        event.editMessageEmbeds(votacion.construirEmbedEnCurso()).queue();
    }
}
