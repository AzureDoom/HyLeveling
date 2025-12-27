package com.azuredoom.hyleveling;

import com.azuredoom.hyleveling.config.ConfigManager;
import com.azuredoom.hyleveling.config.LevelFormulaFactory;
import com.azuredoom.hyleveling.database.DataSourceFactory;
import com.azuredoom.hyleveling.database.JdbcLevelRepository;
import com.azuredoom.hyleveling.level.LevelServiceImpl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class Main {

    public static final System.Logger LOGGER = System.getLogger(Main.class.getName());

    public static Path configPath = Paths.get("./hyleveling");

    static void main() {
        var config = ConfigManager.loadOrCreate(configPath);
        var desc = LevelFormulaFactory.descriptorFromConfig(config);
        var formula = LevelFormulaFactory.fromConfig(config);
        var jdbcUrl = config.database.jdbcUrl;
        var user = config.database.username;
        var pass = config.database.password;
        var maxPoolSize = config.database.maxPoolSize;
        var dataSource = DataSourceFactory.create(jdbcUrl, user, pass, maxPoolSize);
        var repository = new JdbcLevelRepository(dataSource);
        if (config.formula.migrateXP) {
            repository.migrateFormulaIfNeeded(formula, desc);
        }
        var levelService = new LevelServiceImpl(formula, repository);
        var testId = UUID.fromString("d3804858-4bb8-4026-ae21-386255ed467d");

        levelService.addXp(testId, 500);

        LOGGER.log(System.Logger.Level.INFO, String.format("XP: %d", levelService.getXp(testId)));
        LOGGER.log(System.Logger.Level.INFO, String.format("Level: %d", levelService.getLevel(testId)));

        Runtime.getRuntime().addShutdownHook(new Thread(repository::close));
    }
}
