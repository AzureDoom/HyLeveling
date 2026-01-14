package com.azuredoom.levelingcore.rank;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.logging.Level;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.exceptions.LevelingCoreException;

public class RankManager {

    private RankConfig config;

    public void load(Path dataDir) {
        try {
            Files.createDirectories(dataDir);

            var rankPath = dataDir.resolve("ranks.yml");

            if (Files.notExists(rankPath)) {
                LevelingCore.LOGGER.at(Level.INFO).log("No ranks.yml found at {0}", rankPath);
                this.config = null;
                return;
            }

            var opts = new LoaderOptions();
            opts.setMaxAliasesForCollections(50);

            var yaml = new Yaml(new Constructor(RankConfig.class, opts));
            try (var reader = Files.newBufferedReader(rankPath, StandardCharsets.UTF_8)) {
                RankConfig loadedConfig = yaml.load(reader);
                if (loadedConfig == null) {
                    this.config = null;
                    return;
                }
                if (loadedConfig.ranks != null) {
                    loadedConfig.ranks.sort(Comparator.comparingInt(rank -> rank.minLevel));
                }
                this.config = loadedConfig;
                LevelingCore.LOGGER.at(Level.INFO).log("Loaded ranks from {0}", rankPath);
            }
        } catch (Exception e) {
            throw new LevelingCoreException("Failed to load ranks.yml", e);
        }
    }

    public Optional<RankConfig.RankEntry> getRankForLevel(int level) {
        if (config == null || config.ranks == null) {
            return Optional.empty();
        }

        return config.ranks.stream()
            .filter(rank -> level >= rank.minLevel && level <= rank.maxLevel)
            .findFirst();
    }
}
