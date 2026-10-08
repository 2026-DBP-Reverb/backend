package dbp.backend.common.config.database;

import dbp.backend.common.exception.DatabaseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class DatabaseConfig {
    private static final Map<String, String> DOTENV = loadDotenv();

    public static final String DB_DRIVER = required("DB_DRIVER");
    public static final String DB_URL = required("DB_URL");
    public static final String DB_USERNAME = required("DB_USERNAME");
    public static final String DB_PASSWORD = required("DB_PASSWORD");

    private DatabaseConfig() {
    }

    private static String required(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        if (value == null || value.isBlank()) {
            value = DOTENV.get(key);
        }
        if (value == null || value.isBlank()) {
            throw new DatabaseException();
        }
        return value;
    }

    private static Map<String, String> loadDotenv() {
        Path dotenvPath = Path.of(".env");
        if (!Files.exists(dotenvPath)) {
            return Map.of();
        }

        Map<String, String> values = new HashMap<>();
        try {
            for (String line : Files.readAllLines(dotenvPath)) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    continue;
                }

                if (trimmedLine.startsWith("export ")) {
                    trimmedLine = trimmedLine.substring("export ".length()).trim();
                }

                int separatorIndex = trimmedLine.indexOf('=');
                if (separatorIndex <= 0) {
                    continue;
                }

                String key = trimmedLine.substring(0, separatorIndex).trim();
                String value = trimmedLine.substring(separatorIndex + 1).trim();
                values.put(key, unwrapQuote(value));
            }
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
        return values;
    }

    private static String unwrapQuote(String value) {
        if (value.length() < 2) {
            return value;
        }

        char first = value.charAt(0);
        char last = value.charAt(value.length() - 1);
        if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
