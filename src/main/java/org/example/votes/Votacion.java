package org.example.votes;

import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Representa el estado de UNA votación activa: quién la inició, sobre qué
 * usuario es (el que recibiría o perdería el rol si gana), y el registro
 * de votos (un voto por usuario, puede cambiarlo mientras esté activa).
 *
 * También sabe reconstruir su propio embed "en curso" mediante una
 * EmbedFactory que le entrega el comando que la creó (F1, F2, etc.).
 * Así, quien escucha los clics de los botones (VotoButtonListener) no
 * necesita saber de qué tipo de votación se trata.
 */
public class Votacion {

    public enum Opcion { SI, NO }

    private final String mensajeId;
    private final String iniciadorMencion;
    private final String targetUserId;
    private final String targetMencion;
    private final EmbedFactory embedEnCursoFactory;
    private final Map<String, Opcion> votos = new ConcurrentHashMap<>(); // userId -> voto

    public Votacion(String mensajeId, String iniciadorMencion, String targetUserId,
                     String targetMencion, EmbedFactory embedEnCursoFactory) {
        this.mensajeId = mensajeId;
        this.iniciadorMencion = iniciadorMencion;
        this.targetUserId = targetUserId;
        this.targetMencion = targetMencion;
        this.embedEnCursoFactory = embedEnCursoFactory;
    }

    /**
     * Registra el voto de un usuario. Si ya había votado antes, se
     * sobreescribe (le permitimos cambiar de opinión mientras esté activa).
     *
     * @return true si es la primera vez que este usuario vota.
     */
    public boolean votar(String userId, Opcion opcion) {
        return votos.put(userId, opcion) == null;
    }

    public long contar(Opcion opcion) {
        return votos.values().stream().filter(v -> v == opcion).count();
    }

    /** Reconstruye el embed "en curso" con el conteo actual de votos. */
    public MessageEmbed construirEmbedEnCurso() {
        return embedEnCursoFactory.crear(contar(Opcion.SI), contar(Opcion.NO));
    }

    public String getMensajeId() {
        return mensajeId;
    }

    public String getIniciadorMencion() {
        return iniciadorMencion;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public String getTargetMencion() {
        return targetMencion;
    }
}
