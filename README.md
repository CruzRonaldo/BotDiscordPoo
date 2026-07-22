# BotDiscordPoo

Bot de Discord en Java, construido con [JDA](https://github.com/discord-jda/JDA) y organizado en Programación Orientada a Objetos (POO).

## Estructura del proyecto

```
src/main/java/org/example/
├── Main.java                      # Punto de entrada: carga config y arranca el bot
├── config/
│   └── ConfigManager.java         # Lee el token desde config.properties
├── bot/
│   └── DiscordBot.java            # Construye JDA, registra comandos y listeners
├── commands/
│   ├── Command.java                # Interfaz que define el contrato de un comando
│   ├── CommandManager.java         # Registra y despacha los comandos
│   └── impl/
│       └── PingCommand.java        # Ejemplo de comando /ping
└── listeners/
    └── SlashCommandListener.java  # Escucha las interacciones de Discord
```

### Principios de POO aplicados
- **Abstracción / Interfaces**: `Command` define qué debe tener todo comando, sin importar su implementación.
- **Polimorfismo**: `CommandManager` trata a todos los comandos igual (`Command`), sin conocer su clase concreta.
- **Encapsulamiento**: `DiscordBot` esconde los detalles de conexión con JDA; `ConfigManager` esconde cómo se lee el token.
- **Responsabilidad única**: cada clase hace una sola cosa (configuración, conexión, comandos, eventos).

## Configuración

1. Ve a https://discord.com/developers/applications, crea una aplicación (o usa la que ya tienes) y copia el **Bot Token**.
2. Abre `src/main/resources/config.properties` y reemplaza `TU_TOKEN_AQUI` con tu token real.
3. Este archivo ya está en `.gitignore`, así que no se subirá a git accidentalmente.
4. En el Developer Portal, en la pestaña **Bot**, activa el intent **Message Content Intent** (lo usamos en `DiscordBot.java`).
5. Invita al bot a tu servidor con el scope `bot` y `applications.commands`, con los permisos que necesites.

## Ejecutar

Desde IntelliJ: abre `Main.java` y dale Run.

Desde terminal:
```bash
mvn clean package
java -jar target/BotDiscordPoo.jar
```

## Agregar un nuevo comando

1. Crea una clase en `commands/impl/` que implemente `Command` (usa `PingCommand.java` como plantilla).
2. Regístrala en `DiscordBot.java`, dentro de `registrarComandos()`:
   ```java
   registrarComando(new TuNuevoComando());
   ```
3. Los comandos globales pueden tardar hasta 1 hora en aparecer en Discord la primera vez. Para pruebas instantáneas, puedes registrar comandos por servidor (guild) en vez de global — pregúntame si quieres que lo agreguemos.

## Notas
- La versión de Java del `pom.xml` estaba puesta en 26 (no existe / no es estable); la dejé en **21** (LTS), que es totalmente compatible con JDA 5.3.0.
- Se quitó el repositorio Maven personalizado `dv8tion.net` del pom, porque JDA ya está publicado en Maven Central.
- Se agregó `slf4j-simple` porque JDA lo requiere para no mostrar warnings de logging en consola.
- Se agregó el `maven-shade-plugin` para poder generar un `.jar` ejecutable con todas las dependencias incluidas.
