package org.example;

import org.example.bot.DiscordBot;
import org.example.config.ConfigManager;

/**
 * Punto de entrada de la aplicación.
 * Su única responsabilidad es: cargar la configuración y arrancar el bot.
 */
public class Main {

    public static void main(String[] args) {
        try {
            ConfigManager config = new ConfigManager();
            DiscordBot bot = new DiscordBot(config);
            bot.iniciar();
        } catch (Exception e) {
            System.err.println("❌ Error al iniciar el bot: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
