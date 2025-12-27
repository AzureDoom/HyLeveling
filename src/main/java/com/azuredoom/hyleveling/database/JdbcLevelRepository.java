package com.azuredoom.hyleveling.database;

import com.azuredoom.hyleveling.HyLevelingException;
import com.azuredoom.hyleveling.level.PlayerLevelData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;
import javax.sql.DataSource;

/**
 * Implementation of the {@link LevelRepository} interface using JDBC for data persistence. This repository manages
 * the storage and retrieval of player level data in a relational database. The implementation provides methods for
 * saving, loading, checking the existence of, and closing resources related to player level data.
 */
public class JdbcLevelRepository implements LevelRepository {

    private final DataSource dataSource;

    public JdbcLevelRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        createTableIfNotExists();
    }

    /**
     * Ensures the existence of the "player_levels" table in the database. If the table does not already exist,
     * it creates a new table with the necessary schema. The table consists of the following columns:
     * - `player_id`: A unique identifier for the player, represented as a VARCHAR(36), and used as the primary key.
     * - `xp`: A BIGINT column to store the player's experience points (XP), which cannot be null.
     * <p>
     * This method establishes a connection to the database using the provided DataSource and executes
     * a SQL "CREATE TABLE IF NOT EXISTS" statement to create the table if it does not already exist.
     * If any exception occurs during the operation, it wraps and rethrows the exception as a
     * {@link HyLevelingException} to provide a clear context about the failure.
     * <p>
     * Implementation Notes:
     * - Uses a try-with-resources block to ensure the proper closure of the database connection and statement.
     * - The method is private and intended to be used internally within the class during initialization to
     *   guarantee the schema's availability.
     *
     * @throws HyLevelingException if the table creation fails due to database connectivity issues or
     *                             an error in the SQL operation.
     */
    private void createTableIfNotExists() {
        var sql = """
            CREATE TABLE IF NOT EXISTS player_levels (
                player_id VARCHAR(36) PRIMARY KEY,
                xp BIGINT NOT NULL
            )
            """;

        try (
            Connection connection = dataSource.getConnection();
            Statement stmt = connection.createStatement()
        ) {
            stmt.execute(sql);
        } catch (Exception e) {
            throw new HyLevelingException("Failed to create player_levels table", e);
        }
    }

    /**
     * Saves the level-related data for a player. This method updates the player's experience points (XP) in the
     * database if an entry for the player already exists. If no entry exists, a new row is created.
     * <p>
     * The method uses two SQL statements:
     * 1. An "UPDATE" statement to modify the player's existing XP value if the player ID is found.
     * 2. An "INSERT" statement to create a new entry with the player ID and XP if the update operation did not
     *    affect any rows.
     * <p>
     * This method ensures that player XP data is consistently stored in the database and handles scenarios
     * where a player record needs to be either updated or inserted.
     *
     * @param data The {@link PlayerLevelData} instance containing the player's unique identifier and XP value.
     *             The unique identifier is used to check for existing database entries, and the XP value is
     *             saved or updated accordingly.
     * @throws HyLevelingException if any database operation fails, such as connection issues or invalid SQL.
     */
    @Override
    public void save(PlayerLevelData data) {
        var updateSql = "UPDATE player_levels SET xp = ? WHERE player_id = ?";
        var insertSql = "INSERT INTO player_levels (player_id, xp) VALUES (?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            int updated;
            try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                ps.setLong(1, data.getXp());
                ps.setString(2, data.getPlayerId().toString());
                updated = ps.executeUpdate();
            }

            if (updated == 0) {
                try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                    ps.setString(1, data.getPlayerId().toString());
                    ps.setLong(2, data.getXp());
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw new HyLevelingException("Failed to save player level data", e);
        }
    }

    /**
     * Loads the level-related data for a player identified by their unique UUID. This method retrieves the player's
     * experience points (XP) from the database and creates a {@link PlayerLevelData} instance with the retrieved values.
     * If no data exists for the given UUID, the method returns null. If any database-related issue occurs, it wraps
     * and rethrows the exception as a {@link HyLevelingException}.
     *
     * @param id The unique identifier of the player as a {@link UUID}.
     * @return A {@link PlayerLevelData} instance containing the player's XP and unique identifier, or null if no data
     * exists for the given UUID.
     * @throws HyLevelingException if any database operation fails, such as connection issues or invalid SQL.
     */
    @Override
    public PlayerLevelData load(UUID id) {
        var sql = "SELECT xp FROM player_levels WHERE player_id = ?";

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)
        ) {

            ps.setString(1, id.toString());

            var rs = ps.executeQuery();
            if (rs.next()) {
                var data = new PlayerLevelData(id);
                data.setXp(rs.getLong("xp"));
                return data;
            }
            return null;
        } catch (Exception e) {
            throw new HyLevelingException("Failed to load player level data", e);
        }
    }

    /**
     * Checks if a record exists in the "player_levels" database table for the given player UUID.
     * This method executes a SQL query using the provided UUID to determine if an entry exists.
     *
     * @param id The unique identifier of the player as a {@link UUID}.
     * @return {@code true} if a record exists for the given UUID; {@code false} otherwise.
     * @throws HyLevelingException if any database operation fails, such as connection issues or invalid SQL.
     */
    @Override
    public boolean exists(UUID id) {
        var sql = "SELECT 1 FROM player_levels WHERE player_id = ?";

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)
        ) {

            ps.setString(1, id.toString());
            var rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            throw new HyLevelingException("exists() failed", e);
        }
    }

    @Override
    public void close() {
        try {
            if (dataSource instanceof AutoCloseable c) {
                c.close();
            }
        } catch (Exception e) {
            throw new HyLevelingException("Failed to close JDBC datasource", e);
        }
    }
}
