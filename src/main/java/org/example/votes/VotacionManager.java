package org.example.votes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Administra todas las votaciones activas del bot (puede haber varias al
 * mismo tiempo, en distintos canales). Se encarga de:
 * - Guardarlas en memoria mientras están activas (clave = id del mensaje).
 * - Programar su cierre automático tras la duración configurada.
 *
 * Al igual que CommandManager, es un "administrador" central: no contiene
 * la lógica de qué hacer cuando termina la votación, solo la orquesta.
 */
public class VotacionManager {

    /** Máximo de votaciones que pueden estar activas al mismo tiempo en todo el bot. */
    private static final int MAX_VOTACIONES_ACTIVAS = 4;

    private final Map<String, Votacion> votacionesActivas = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    /**
     * @return true si todavía hay espacio para iniciar una votación más
     * (menos de MAX_VOTACIONES_ACTIVAS activas en este momento).
     */
    public boolean hayEspacioParaNuevaVotacion() {
        return votacionesActivas.size() < MAX_VOTACIONES_ACTIVAS;
    }

    /**
     * Registra una votación como activa y programa su cierre.
     *
     * @param votacion         la votación recién creada.
     * @param duracionSegundos cuánto tiempo estará abierta.
     * @param alFinalizar      callback que se ejecuta cuando el tiempo termina
     *                         (aquí es donde el comando actualiza el mensaje con el resultado).
     */
    public void iniciarVotacion(Votacion votacion, long duracionSegundos, Consumer<Votacion> alFinalizar) {
        votacionesActivas.put(votacion.getMensajeId(), votacion);

        scheduler.schedule(() -> {
            votacionesActivas.remove(votacion.getMensajeId());
            alFinalizar.accept(votacion);
        }, duracionSegundos, TimeUnit.SECONDS);
    }

    /**
     * @return la votación activa asociada a ese mensaje, o null si ya
     * terminó o nunca existió.
     */
    public Votacion obtener(String mensajeId) {
        return votacionesActivas.get(mensajeId);
    }
}
