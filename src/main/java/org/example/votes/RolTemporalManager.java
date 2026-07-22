package org.example.votes;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Se encarga de asignar un rol a un miembro y programar su remoción
 * automática tras una duración determinada. También permite consultar si
 * un usuario tiene el rol activo ahora mismo, y quitárselo antes de tiempo
 * (esto último lo usa /f2, que depende de que /f1 ya se haya ganado).
 * <p>
 * Nota: al ser en memoria, si el bot se reinicia antes de que se cumpla el
 * tiempo, el rol NO se quitará automáticamente y tieneRolActivo() olvidará
 * que ese usuario lo tenía. Para producción, lo ideal sería persistir la
 * fecha de expiración (por ejemplo en un archivo o base de datos) y
 * revisarla al arrancar el bot. Si más adelante quieres que lo agreguemos,
 * avísame.
 */
public class RolTemporalManager {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    /** userId -> tarea programada que le quitará el rol. Permite cancelarla si F2 gana antes de tiempo. */
    private final Map<String, ScheduledFuture<?>> remocionesPendientes = new ConcurrentHashMap<>();

    /**
     * Asigna el rol al miembro indicado y programa su remoción automática.
     *
     * @param guild            el servidor donde ocurre la asignación.
     * @param member           el miembro que recibirá el rol.
     * @param rol              el rol a asignar.
     * @param duracionSegundos cuánto tiempo mantendrá el rol antes de que se le quite.
     */
    public void asignarRolTemporal(Guild guild, Member member, Role rol, long duracionSegundos) {
        guild.addRoleToMember(member, rol).queue(
                exito -> {
                    ScheduledFuture<?> tarea = scheduler.schedule(() -> {
                        guild.removeRoleFromMember(member, rol).queue();
                        remocionesPendientes.remove(member.getId());
                    }, duracionSegundos, TimeUnit.SECONDS);

                    remocionesPendientes.put(member.getId(), tarea);
                },
                error -> System.err.println("❌ No se pudo asignar el rol: " + error.getMessage())
        );
    }

    /**
     * @return true si ese usuario tiene actualmente un rol temporal activo
     * otorgado por este manager (es decir, aún no le llegó su hora de
     * remoción y nadie se lo quitó antes de tiempo).
     */
    public boolean tieneRolActivo(String userId) {
        return remocionesPendientes.containsKey(userId);
    }

    /**
     * Quita el rol antes de tiempo (usado por /f2) y cancela la remoción
     * automática que ya estaba programada, para no intentar quitarlo dos veces.
     */
    public void quitarRolAntesDeTiempo(Guild guild, Member member, Role rol) {
        ScheduledFuture<?> tarea = remocionesPendientes.remove(member.getId());
        if (tarea != null) {
            tarea.cancel(false);
        }
        guild.removeRoleFromMember(member, rol).queue();
    }
}
