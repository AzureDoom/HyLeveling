package com.azuredoom.hyleveling.database;

import com.azuredoom.hyleveling.HyLevelingException;
import com.azuredoom.hyleveling.level.PlayerLevelData;

import java.sql.*;
import java.util.UUID;

/**
 * Implementation of the {@link LevelRepository} interface for managing player level data in an H2 database. This class
 * provides functionality to store, retrieve, and check the existence of player level data, as well as handling the
 * creation of the required database schema if it does not already exist.
 * <p>
 * The H2 database connection is established when an instance is created and should be properly closed using the
 * {@link #close()} method to release resources.
 */
public class H2LevelRepository implements LevelRepository {

    private final Connection connection;

    public H2LevelRepository(String databasePath) {
        try {
            connection = DriverManager.getConnection("jdbc:h2:" + databasePath + ";AUTO_SERVER=TRUE");
            createTableIfNotExists();
        } catch (SQLException e) {
            throw new HyLevelingException("Failed to connect to H2 database", e);
        }
    }

    /**
     * Creates the database table `player_levels` if it does not already exist. This method ensures that the required
     * schema for storing player level data is available in the H2 database. The table has the following structure: -
     * `player_id` (VARCHAR(36)): Primary key, represents the unique identifier of a player. - `xp` (BIGINT): Non-null,
     * represents the experience points of the player. This method uses an SQL CREATE TABLE statement with the `IF NOT
     * EXISTS` clause to ensure that the operation is idempotent and avoids re-creating the table if it already exists
     * in the database.
     *
     * @throws SQLException if an error occurs during the table creation process.
     */
    private void createTableIfNotExists() throws SQLException {
        var sql = """
                CREATE TABLE IF NOT EXISTS player_levels (
                    player_id VARCHAR(36) PRIMARY KEY,
                    xp BIGINT NOT NULL
                );
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Saves the player's level data into the database. This method uses an SQL MERGE statement to insert or update the
     * player's experience points associated with their unique identifier.
     *
     * @param data the PlayerLevelData object containing the player's unique ID and experience points
     * @throws HyLevelingException if an SQL error occurs while executing the save operation
     */
    @Override
    public void save(PlayerLevelData data) {
        var sql = """
                MERGE INTO player_levels KEY(player_id) VALUES(?, ?)
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, data.getPlayerId().toString());
            ps.setLong(2, data.getXp());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new HyLevelingException("Failed to save player level data", e);
        }
    }

    /**
     * Loads the level data of a player by their unique identifier from the database. This method queries the
     * `player_levels` table to retrieve the stored experience points (XP) for the player and creates a
     * {@link PlayerLevelData} object.
     *
     * @param id the UUID of the player whose level data is to be loaded
     * @return a {@link PlayerLevelData} object containing the player's level data, or {@code null} if no data is found
     *         for the given player
     * @throws HyLevelingException if an SQL error occurs while querying the database
     */
    @Override
    public PlayerLevelData load(UUID id) {
        var sql = """
                SELECT xp FROM player_levels WHERE player_id = ?
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id.toString());

            var rs = ps.executeQuery();
            if (rs.next()) {
                PlayerLevelData data = new PlayerLevelData(id);
                data.setXp(rs.getLong("xp"));
                return data;
            }
        } catch (SQLException e) {
            throw new HyLevelingException("Failed to load player level data", e);
        }

        return null;
    }

    /**
     * Checks if a record exists in the `player_levels` table for the given player ID. This method executes a query to
     * verify the existence of an entry in the database.
     *
     * @param id the UUID of the player whose existence is to be checked
     * @return {@code true} if a record exists for the player ID, {@code false} otherwise
     * @throws HyLevelingException if an error occurs while interacting with the database
     */
    @Override
    public boolean exists(UUID id) {
        var sql = """
                SELECT 1 FROM player_levels WHERE player_id = ?
            """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            var rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new HyLevelingException("H2 exists() failed", e);
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (Exception e) {
            throw new HyLevelingException("Failed to close H2 connection", e);
        }
    }
}
