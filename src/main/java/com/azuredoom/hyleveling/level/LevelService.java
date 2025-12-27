package com.azuredoom.hyleveling.level;

import com.azuredoom.hyleveling.events.LevelDownListener;
import com.azuredoom.hyleveling.events.XpGainListener;
import com.azuredoom.hyleveling.events.XpLossListener;

import java.util.UUID;

/**
 * Service interface for managing player levels and experience points (XP) within a leveling system. This interface
 * provides methods for retrieving and modifying XP, as well as managing listeners for level-related events.
 */
public interface LevelService {

    int getLevel(UUID playerId);

    long getXp(UUID playerId);

    void addXp(UUID playerId, long amount);

    void removeXp(UUID playerId, long amount);

    void setXp(UUID playerId, long xp);

    void registerListener(LevelListener listener);

    void unregisterListener(LevelListener listener);

    void registerXpGainListener(XpGainListener listener);

    void registerXpLossListener(XpLossListener listener);

    void registerLevelDownListener(LevelDownListener listener);
}
