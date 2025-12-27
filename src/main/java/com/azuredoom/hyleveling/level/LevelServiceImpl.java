package com.azuredoom.hyleveling.level;

import com.azuredoom.hyleveling.database.LevelRepository;
import com.azuredoom.hyleveling.events.LevelDownListener;
import com.azuredoom.hyleveling.events.XpGainListener;
import com.azuredoom.hyleveling.events.XpLossListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the LevelService interface for managing player levels and experience points (XP). This class is
 * responsible for handling the retrieval, modification, and storage of player level data, as well as notifying
 * registered listeners for level-up, level-down, XP gain, and XP loss events.
 */
public class LevelServiceImpl implements LevelService {

    private final LevelFormula formula;

    private final LevelRepository repository;

    private final Map<UUID, PlayerLevelData> cache = new ConcurrentHashMap<>();

    private final List<LevelListener> levelUpListeners = new ArrayList<>();

    private final List<LevelDownListener> levelDownListeners = new ArrayList<>();

    private final List<XpGainListener> xpGainListeners = new ArrayList<>();

    private final List<XpLossListener> xpLossListeners = new ArrayList<>();

    public LevelServiceImpl(LevelFormula formula, LevelRepository repository) {
        this.formula = formula;
        this.repository = repository;
    }

    /**
     * Retrieves the {@link PlayerLevelData} associated with the given player ID. If the player data is not present in
     * the cache, it attempts to load it from the repository. If the repository does not contain data for the given ID,
     * a new instance of {@link PlayerLevelData} is created and cached.
     *
     * @param id The unique identifier (UUID) of the player whose level data is being retrieved.
     * @return The {@link PlayerLevelData} associated with the given player ID.
     */
    private PlayerLevelData get(UUID id) {
        return cache.computeIfAbsent(id, uuid -> {
            var stored = repository.load(uuid);
            return stored != null ? stored : new PlayerLevelData(uuid);
        });
    }

    /**
     * Retrieves the total experience points (XP) of the player associated with the given unique identifier (UUID).
     *
     * @param id The unique identifier (UUID) of the player whose experience points are being retrieved.
     * @return The total XP of the player as a long.
     */
    @Override
    public long getXp(UUID id) {
        return get(id).getXp();
    }

    /**
     * Retrieves the level of a player based on their current experience points (XP).
     *
     * @param id The unique identifier (UUID) of the player whose level is being retrieved.
     * @return The player's level as an integer, calculated from their XP.
     */
    @Override
    public int getLevel(UUID id) {
        return formula.getLevelForXp(get(id).getXp());
    }

    /**
     * Adds a specified amount of experience points (XP) to the player associated with the given ID. If adding XP
     * results in the player leveling up, the appropriate level-up events are triggered. Notifications are sent to
     * registered listeners for both XP gain and level-up events, if applicable.
     *
     * @param id     The unique identifier (UUID) of the player whose XP is being modified.
     * @param amount The amount of XP to be added to the player's current XP balance.
     */
    @Override
    public void addXp(UUID id, long amount) {
        var data = get(id);
        var oldLevel = getLevel(id);

        data.setXp(data.getXp() + amount);
        repository.save(data);

        xpGainListeners.forEach(l -> l.onXpGain(id, amount));

        var newLevel = getLevel(id);
        if (newLevel > oldLevel) {
            levelUpListeners.forEach(l -> l.onLevelUp(id, newLevel));
        }
    }

    /**
     * Removes a specified amount of experience points (XP) from the player identified by the given ID. If the reduction
     * in XP results in a decrease in the player's level, the appropriate level-down events are triggered.
     *
     * @param id     The unique identifier (UUID) of the player whose XP is being reduced.
     * @param amount The amount of XP to remove from the player's total.
     */
    @Override
    public void removeXp(UUID id, long amount) {
        var data = get(id);
        var oldLevel = getLevel(id);

        data.setXp(data.getXp() - amount);
        repository.save(data);

        xpLossListeners.forEach(l -> l.onXpLoss(id, amount));

        var newLevel = getLevel(id);
        if (newLevel < oldLevel) {
            levelDownListeners.forEach(l -> l.onLevelDown(id, newLevel));
        }
    }

    /**
     * Sets the experience points (XP) of a player to a specified value. If the new XP value results in a level change,
     * the appropriate level-up or level-down listeners are triggered accordingly.
     *
     * @param id The unique identifier (UUID) of the player whose XP is being set.
     * @param xp The new experience points (XP) value to assign to the player.
     */
    @Override
    public void setXp(UUID id, long xp) {
        var data = get(id);
        var oldLevel = getLevel(id);

        data.setXp(xp);
        repository.save(data);

        var newLevel = getLevel(id);
        if (newLevel > oldLevel) {
            levelUpListeners.forEach(l -> l.onLevelUp(id, newLevel));
        } else if (newLevel < oldLevel) {
            levelDownListeners.forEach(l -> l.onLevelDown(id, newLevel));
        }
    }

    /**
     * Registers a {@link LevelListener} to be notified of player level-up events. The registered listener's
     * {@code onLevelUp} method will be triggered when a player's level increases.
     *
     * @param listener The {@link LevelListener} to be registered for receiving notifications about level-up events.
     */
    @Override
    public void registerListener(LevelListener listener) {
        levelUpListeners.add(listener);
    }

    /**
     * Unregisters a previously registered {@link LevelListener}. After this method is invoked, the specified listener
     * will no longer receive notifications about player level-up events.
     *
     * @param listener The {@link LevelListener} to be unregistered from receiving level-up notifications.
     */
    @Override
    public void unregisterListener(LevelListener listener) {
        levelUpListeners.remove(listener);
    }

    /**
     * Registers a listener to be notified of events when a player levels down. The listener's {@code onLevelDown}
     * method will be triggered whenever a player's level is decreased due to a specific action or condition in the
     * system.
     *
     * @param listener The {@link LevelDownListener} to be registered for receiving level-down notifications.
     */
    @Override
    public void registerLevelDownListener(LevelDownListener listener) {
        levelDownListeners.add(listener);
    }

    /**
     * Registers a listener to be notified of events when a player gains experience points (XP). The listener's
     * {@code onXpGain} method will be triggered whenever a player earns XP in the system.
     *
     * @param listener The {@link XpGainListener} to be registered for receiving XP gain notifications.
     */
    @Override
    public void registerXpGainListener(XpGainListener listener) {
        xpGainListeners.add(listener);
    }

    /**
     * Registers a listener to be notified of events when a player loses experience points (XP). The listener's
     * {@code onXpLoss} method will be triggered whenever a player loses XP in the system.
     *
     * @param listener The {@link XpLossListener} to be registered for receiving XP loss notifications.
     */
    @Override
    public void registerXpLossListener(XpLossListener listener) {
        xpLossListeners.add(listener);
    }
}
