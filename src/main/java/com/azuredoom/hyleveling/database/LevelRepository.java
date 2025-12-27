package com.azuredoom.hyleveling.database;

import com.azuredoom.hyleveling.level.PlayerLevelData;

import java.util.UUID;

/**
 * Repository interface for managing player level data. This interface provides methods to save, load, check existence,
 * and close resources related to player level data storage.
 */
public interface LevelRepository {

    void save(PlayerLevelData data);

    PlayerLevelData load(UUID id);

    boolean exists(UUID id);

    void close();
}
