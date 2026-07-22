package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Se encarga de cargar la configuración del bot (por ejemplo, el token)
 * desde el archivo src/main/resources/config.properties.
 * Esto evita tener el token escrito directamente en el código fuente,
 * lo cual es una mala práctica y un riesgo de seguridad (sobre todo si
 * subes el proyecto a un repositorio público como GitHub).
 */
public class ConfigManager {

    private final Properties properties;

    public ConfigManager() {
        this.properties = new Properties();
        cargarConfiguracion();
    }

    private void cargarConfiguracion() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IllegalStateException(
                        "No se encontró el archivo config.properties en src/main/resources. " +
                        "Copia config.properties.example, renómbralo y agrega tu token."
                );
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Error al leer config.properties", e);
        }
    }

    public String getToken() {
        String token = properties.getProperty("discord.token");
        if (token == null || token.isBlank() || token.equals("TU_TOKEN_AQUI")) {
            throw new IllegalStateException(
                    "El token de Discord no está configurado. Edita config.properties y coloca tu token real."
            );
        }
        return token;
    }

    public String getRoleIdF1() {
        String roleId = properties.getProperty("f1.role.id");
        if (roleId == null || roleId.isBlank() || roleId.equals("TU_ROLE_ID_AQUI")) {
            throw new IllegalStateException(
                    "El ID del rol de F1 no está configurado. Edita config.properties y coloca el ID real del rol."
            );
        }
        return roleId;
    }

    public String getUrlDescarga() {
        String url = properties.getProperty("descarga.url");
        if (url == null || url.isBlank() || url.equals("TU_URL_AQUI")) {
            throw new IllegalStateException(
                    "El enlace de descarga no está configurado. Edita config.properties y coloca la URL real."
            );
        }
        return url;
    }
}
