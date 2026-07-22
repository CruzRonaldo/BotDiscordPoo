package org.example.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.example.commands.Command;
import org.example.commands.CommandManager;
import org.example.commands.impl.PingCommand;
import org.example.commands.impl.HelpCommand;
import org.example.commands.impl.AislarUsuarioCommand;
import org.example.commands.impl.DesaislarUsuarioCommand;
import org.example.commands.impl.ModSmashCommand;
import org.example.config.ConfigManager;
import org.example.listeners.SlashCommandListener;
import org.example.listeners.VotoButtonListener;
import org.example.votes.RolTemporalManager;
import org.example.votes.VotacionManager;

/**
 * Encapsula todo lo relacionado a la conexión con Discord: construir la
 * instancia de JDA, registrar comandos y listeners, y arrancar el bot.
 * <p>
 * Es el punto central que "arma" el bot; Main solo se encarga de instanciar
 * esta clase y llamar a start().
 */
public class DiscordBot {

    private final ConfigManager config;
    private final CommandManager commandManager;
    private final VotacionManager votacionManager;
    private final RolTemporalManager rolTemporalManager;
    private JDA jda;

    public DiscordBot(ConfigManager config) {
        this.config = config;
        this.commandManager = new CommandManager();
        this.votacionManager = new VotacionManager();
        this.rolTemporalManager = new RolTemporalManager();
    }

    /**
     * Registra aquí todos los comandos disponibles del bot.
     * Para agregar uno nuevo: crea la clase en commands.impl (implementando
     * Command) y añade una línea "registrarComando(new TuComando());".
     */
    private void registrarComandos() {
        registrarComando(new PingCommand());
        registrarComando(new HelpCommand(commandManager));
        registrarComando(new AislarUsuarioCommand(votacionManager, rolTemporalManager, config.getRoleIdF1()));
        registrarComando(new DesaislarUsuarioCommand(votacionManager, rolTemporalManager, config.getRoleIdF1()));
        registrarComando(new ModSmashCommand(config.getUrlDescarga()));
        // registrarComando(new OtroComando());
    }

    private void registrarComando(Command command) {
        commandManager.registrar(command);
    }

    /**
     * Construye la instancia de JDA, conecta con Discord y sube los
     * comandos slash. Bloquea hasta que la conexión quede lista (READY).
     */
    public void iniciar() throws InterruptedException {
        registrarComandos();

        jda = JDABuilder.createDefault(config.getToken())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                .setActivity(Activity.playing("Escribe /help para ver lista de comandos."))
                .addEventListeners(
                        new SlashCommandListener(commandManager),
                        new VotoButtonListener(votacionManager)
                )
                .build();

        jda.awaitReady();

        commandManager.registrarComandosEnDiscord(jda);

        System.out.println("✅ Bot conectado como: " + jda.getSelfUser().getAsTag());
    }

    public JDA getJda() {
        return jda;
    }
}