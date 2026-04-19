package net.enelson.sopprefix.prefix;

import net.enelson.sopprefix.SopPrefixPlugin;
import net.enelson.sopli.lib.database.DatabaseConfig;
import net.enelson.sopli.lib.database.SopDatabase;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class PrefixDatabaseStorage {

    private final SopPrefixPlugin plugin;
    private final SopDatabase database;
    private final String tablePrefix;

    PrefixDatabaseStorage(SopPrefixPlugin plugin, SopDatabase database, String tablePrefix) {
        this.plugin = plugin;
        this.database = database;
        this.tablePrefix = tablePrefix;
    }

    static DatabaseConfig createConfig(SopPrefixPlugin plugin) {
        String path = "settings.database.";
        return DatabaseConfig.mysql(
                        plugin.getConfig().getString(path + "host", "127.0.0.1"),
                        plugin.getConfig().getInt(path + "port", 3306),
                        plugin.getConfig().getString(path + "database", "minecraft"))
                .credentials(
                        plugin.getConfig().getString(path + "username", "root"),
                        plugin.getConfig().getString(path + "password", ""))
                .poolName(plugin.getConfig().getString(path + "pool-name", "SopPrefix"))
                .maximumPoolSize(plugin.getConfig().getInt(path + "maximum-pool-size", 10))
                .minimumIdle(plugin.getConfig().getInt(path + "minimum-idle", 2))
                .connectionTimeout(plugin.getConfig().getLong(path + "connection-timeout", 30000L))
                .idleTimeout(plugin.getConfig().getLong(path + "idle-timeout", 600000L))
                .maxLifetime(plugin.getConfig().getLong(path + "max-lifetime", 1800000L))
                .property("cachePrepStmts", "true")
                .property("prepStmtCacheSize", "250")
                .property("prepStmtCacheSqlLimit", "2048")
                .property("useServerPrepStmts", "true")
                .build();
    }

    void initialize() throws SQLException {
        database.execute("CREATE TABLE IF NOT EXISTS " + table("categories") + " ("
                + "id VARCHAR(191) NOT NULL,"
                + "name TEXT NOT NULL,"
                + "material VARCHAR(255) NOT NULL,"
                + "slot INT NOT NULL DEFAULT 0,"
                + "lore TEXT NULL,"
                + "PRIMARY KEY (id)"
                + ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");

        database.execute("CREATE TABLE IF NOT EXISTS " + table("definitions") + " ("
                + "segment_id VARCHAR(191) NOT NULL,"
                + "id VARCHAR(191) NOT NULL,"
                + "display_name TEXT NOT NULL,"
                + "value_text TEXT NOT NULL,"
                + "category_id VARCHAR(191) NOT NULL,"
                + "material VARCHAR(255) NOT NULL,"
                + "permission VARCHAR(255) NOT NULL,"
                + "lore TEXT NULL,"
                + "PRIMARY KEY (segment_id, id)"
                + ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");

        database.execute("CREATE TABLE IF NOT EXISTS " + table("formats") + " ("
                + "segment_id VARCHAR(191) NOT NULL,"
                + "id VARCHAR(191) NOT NULL,"
                + "display_name TEXT NOT NULL,"
                + "format_text TEXT NOT NULL,"
                + "material VARCHAR(255) NOT NULL,"
                + "permission VARCHAR(255) NOT NULL,"
                + "lore TEXT NULL,"
                + "PRIMARY KEY (segment_id, id)"
                + ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
    }

    void syncFromConfig(List<TagSegment> editableSegments) throws SQLException {
        syncCategoriesFromConfig();
        for (TagSegment segment : editableSegments) {
            syncDefinitionsFromConfig(segment);
            syncFormatsFromConfig(segment);
        }
    }

    List<PrefixCategory> loadCategories() throws SQLException {
        return database.withConnection(connection -> {
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            List<PrefixCategory> result = new ArrayList<PrefixCategory>();
            try {
                statement = connection.prepareStatement("SELECT id, name, material, slot, lore FROM " + table("categories") + " ORDER BY slot ASC, id ASC");
                resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    result.add(new PrefixCategory(
                            resultSet.getString("id").toLowerCase(),
                            resultSet.getString("name"),
                            resultSet.getString("material"),
                            resultSet.getInt("slot"),
                            deserializeLore(resultSet.getString("lore"))
                    ));
                }
                return result;
            } finally {
                closeQuietly(resultSet);
                closeQuietly(statement);
            }
        });
    }

    List<PrefixDefinition> loadDefinitions(TagSegment segment) throws SQLException {
        return database.withConnection(connection -> {
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            List<PrefixDefinition> result = new ArrayList<PrefixDefinition>();
            try {
                statement = connection.prepareStatement("SELECT id, display_name, value_text, category_id, material, permission, lore "
                        + "FROM " + table("definitions") + " WHERE segment_id = ? ORDER BY id ASC");
                statement.setString(1, segment.getId());
                resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    result.add(new PrefixDefinition(
                            resultSet.getString("id").toLowerCase(),
                            segment.getId(),
                            resultSet.getString("display_name"),
                            resultSet.getString("value_text"),
                            resultSet.getString("category_id").toLowerCase(),
                            resultSet.getString("material"),
                            resultSet.getString("permission"),
                            deserializeLore(resultSet.getString("lore"))
                    ));
                }
                return result;
            } finally {
                closeQuietly(resultSet);
                closeQuietly(statement);
            }
        });
    }

    List<FormatDefinition> loadFormats(TagSegment segment) throws SQLException {
        return database.withConnection(connection -> {
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            List<FormatDefinition> result = new ArrayList<FormatDefinition>();
            try {
                statement = connection.prepareStatement("SELECT id, display_name, format_text, material, permission, lore "
                        + "FROM " + table("formats") + " WHERE segment_id = ? ORDER BY id ASC");
                statement.setString(1, segment.getId());
                resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    result.add(new FormatDefinition(
                            resultSet.getString("id").toLowerCase(),
                            segment.getId(),
                            resultSet.getString("display_name"),
                            resultSet.getString("format_text"),
                            resultSet.getString("material"),
                            resultSet.getString("permission"),
                            deserializeLore(resultSet.getString("lore"))
                    ));
                }
                return result;
            } finally {
                closeQuietly(resultSet);
                closeQuietly(statement);
            }
        });
    }

    private void syncCategoriesFromConfig() throws SQLException {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("categories");
        if (section == null) {
            return;
        }
        database.withConnection(connection -> {
            PreparedStatement statement = null;
            try {
                statement = connection.prepareStatement("INSERT INTO " + table("categories")
                        + " (id, name, material, slot, lore) VALUES (?, ?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE name = VALUES(name), material = VALUES(material), slot = VALUES(slot), lore = VALUES(lore)");
                for (String id : section.getKeys(false)) {
                    ConfigurationSection categorySection = section.getConfigurationSection(id);
                    if (categorySection == null) {
                        continue;
                    }
                    statement.setString(1, id.toLowerCase());
                    statement.setString(2, categorySection.getString("name", id));
                    statement.setString(3, categorySection.getString("material", Material.BOOK.name()));
                    statement.setInt(4, categorySection.getInt("slot", 0));
                    statement.setString(5, serializeLore(categorySection.getStringList("lore")));
                    statement.addBatch();
                }
                statement.executeBatch();
            } finally {
                closeQuietly(statement);
            }
        });
    }

    private void syncDefinitionsFromConfig(TagSegment segment) throws SQLException {
        if (segment.getTextSection() == null || segment.getTextSection().trim().isEmpty()) {
            return;
        }
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(segment.getTextSection());
        if (section == null) {
            return;
        }
        database.withConnection(connection -> {
            PreparedStatement statement = null;
            try {
                statement = connection.prepareStatement("INSERT INTO " + table("definitions")
                        + " (segment_id, id, display_name, value_text, category_id, material, permission, lore) VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE display_name = VALUES(display_name), value_text = VALUES(value_text), category_id = VALUES(category_id), material = VALUES(material), permission = VALUES(permission), lore = VALUES(lore)");
                for (String id : section.getKeys(false)) {
                    ConfigurationSection definitionSection = section.getConfigurationSection(id);
                    if (definitionSection == null) {
                        continue;
                    }
                    statement.setString(1, segment.getId());
                    statement.setString(2, id.toLowerCase());
                    statement.setString(3, definitionSection.getString("display-name", id));
                    statement.setString(4, definitionSection.getString("value", id));
                    statement.setString(5, definitionSection.getString("category", "common").toLowerCase());
                    statement.setString(6, definitionSection.getString("material", Material.PAPER.name()));
                    statement.setString(7, definitionSection.getString("permission", ""));
                    statement.setString(8, serializeLore(definitionSection.getStringList("lore")));
                    statement.addBatch();
                }
                statement.executeBatch();
            } finally {
                closeQuietly(statement);
            }
        });
    }

    private void syncFormatsFromConfig(TagSegment segment) throws SQLException {
        if (segment.getFormatSection() == null || segment.getFormatSection().trim().isEmpty()) {
            return;
        }
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(segment.getFormatSection());
        if (section == null) {
            return;
        }
        database.withConnection(connection -> {
            PreparedStatement statement = null;
            try {
                statement = connection.prepareStatement("INSERT INTO " + table("formats")
                        + " (segment_id, id, display_name, format_text, material, permission, lore) VALUES (?, ?, ?, ?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE display_name = VALUES(display_name), format_text = VALUES(format_text), material = VALUES(material), permission = VALUES(permission), lore = VALUES(lore)");
                for (String id : section.getKeys(false)) {
                    ConfigurationSection formatSection = section.getConfigurationSection(id);
                    if (formatSection == null) {
                        continue;
                    }
                    statement.setString(1, segment.getId());
                    statement.setString(2, id.toLowerCase());
                    statement.setString(3, formatSection.getString("display-name", id));
                    statement.setString(4, formatSection.getString("format", "%value%"));
                    statement.setString(5, formatSection.getString("material", Material.NAME_TAG.name()));
                    statement.setString(6, formatSection.getString("permission", ""));
                    statement.setString(7, serializeLore(formatSection.getStringList("lore")));
                    statement.addBatch();
                }
                statement.executeBatch();
            } finally {
                closeQuietly(statement);
            }
        });
    }

    private String table(String suffix) {
        return tablePrefix + suffix;
    }

    private String serializeLore(List<String> lore) {
        if (lore == null || lore.isEmpty()) {
            return "";
        }
        return String.join("\n", lore);
    }

    private List<String> deserializeLore(String lore) {
        if (lore == null || lore.isEmpty()) {
            return Collections.emptyList();
        }
        String[] split = lore.split("\n", -1);
        List<String> result = new ArrayList<String>(split.length);
        Collections.addAll(result, split);
        return result;
    }

    private void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }
}
