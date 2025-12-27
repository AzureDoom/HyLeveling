package com.azuredoom.hyleveling.level;

import java.util.UUID;

/**
 * Interface for receiving notifications about player level-up events in a leveling system. This listener provides a
 * callback method triggered whenever a player's level increases. Implementations can use this to perform actions such
 * as granting rewards or triggering in-game events.
 */
public interface LevelListener {

    void onLevelUp(UUID playerId, int newLevel);
}
