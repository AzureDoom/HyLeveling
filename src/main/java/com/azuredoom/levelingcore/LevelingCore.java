package com.azuredoom.levelingcore;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.*;
import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.commands.*;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.config.internal.ConfigBootstrap;
import com.azuredoom.levelingcore.events.GainXPEventSystem;
import com.azuredoom.levelingcore.events.LossXPEventSystem;
import com.azuredoom.levelingcore.exceptions.LevelingCoreException;
import com.azuredoom.levelingcore.level.LevelServiceImpl;
import com.azuredoom.levelingcore.rank.RankManager;

public class LevelingCore extends JavaPlugin {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final Path configPath = Paths.get("./mods/levelingcore_LevelingCore/data/config/");

    public static final ConfigBootstrap.Bootstrap bootstrap = ConfigBootstrap.bootstrap(configPath);

    public static LevelServiceImpl levelingService;

    public static RankManager rankManager;

    private static LevelingCore INSTANCE;

    private final Config<GUIConfig> config;

    /**
     * Constructs a new {@code LevelingCore} instance and initializes the core components of the leveling system. This
     * constructor takes a non-null {@link JavaPluginInit} object to set up the necessary dependencies and
     * configurations required for the leveling system to function.
     *
     * @param init a {@link JavaPluginInit} instance used to initialize the plugin environment and dependencies. Must
     *             not be {@code null}.
     */
    public LevelingCore(@Nonnull JavaPluginInit init) {
        super(init);
        INSTANCE = this;
        config = this.withConfig("levelingcore", GUIConfig.CODEC);
    }

    /**
     * Initializes the core components of the leveling system. This method sets up necessary configurations, registers
     * commands, and configures systems to handle player leveling and experience management. It also initializes the
     * singleton instance of the {@code LevelingCore} class.
     */
    @Override
    protected void setup() {
        INSTANCE = this;
        this.config.save();
        LOGGER.at(Level.INFO).log("Leveling Core initializing");
        levelingService = bootstrap.service();
        rankManager = new RankManager();
        rankManager.load(configPath);
        getCommandRegistry().registerCommand(new AddLevelCommand());
        getCommandRegistry().registerCommand(new CheckLevelCommand());
        getCommandRegistry().registerCommand(new AddXpCommand());
        getCommandRegistry().registerCommand(new SetLevelCommand());
        getCommandRegistry().registerCommand(new RemoveLevelCommand());
        getCommandRegistry().registerCommand(new RemoveXpCommand());
        getEntityStoreRegistry().registerSystem(new GainXPEventSystem(config));
        getEntityStoreRegistry().registerSystem(new LossXPEventSystem(config));
    }

    /**
     * Shuts down the {@code LevelingCore} instance and releases allocated resources. This method performs cleanup
     * operations required to properly terminate the leveling system. It includes closing any resources associated with
     * the {@code bootstrap} object and logging the shutdown process.
     *
     * @throws LevelingCoreException if resource cleanup fails.
     */
    @Override
    protected void shutdown() {
        super.shutdown();
        LOGGER.at(Level.INFO).log("Leveling Core shutting down");
        try {
            LevelingCore.bootstrap.closeable().close();
        } catch (Exception e) {
            throw new LevelingCoreException("Failed to close resources", e);
        }
    }

    /**
     * Retrieves the {@link LevelServiceImpl} instance managed by the {@code LevelingCore} class. The
     * {@code LevelService} provides methods for managing player levels and experience points (XP).
     *
     * @return the {@link LevelServiceImpl} instance used by the leveling system.
     */
    public static LevelServiceImpl getLevelService() {
        return levelingService;
    }

    public static RankManager getRankManager() {
        return rankManager;
    }

    /**
     * Provides access to the singleton instance of the {@code LevelingCore} class. This instance serves as the primary
     * entry point for managing the core functionality of the leveling system, including initialization, configuration,
     * and lifecycle management.
     *
     * @return the singleton instance of {@code LevelingCore}.
     */
    public static LevelingCore getInstance() {
        return INSTANCE;
    }
}
