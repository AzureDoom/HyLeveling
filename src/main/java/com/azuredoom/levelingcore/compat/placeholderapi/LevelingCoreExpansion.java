package com.azuredoom.levelingcore.compat.placeholderapi;

import at.helpch.placeholderapi.expansion.PlaceholderExpansion;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.azuredoom.levelingcore.api.LevelingCoreApi;

public class LevelingCoreExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "LevelingCore";
    }

    @Override
    public @NotNull String getAuthor() {
        return "AzureDoom";
    }

    @Override
    public @NotNull String getVersion() {
        return "0.9.4";
    }

    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(PlayerRef playerRef, @NotNull String params) {
        var levelService = LevelingCoreApi.getLevelServiceIfPresent().orElse(null);
        if (levelService == null) {
            return null;
        }
        if (params.equalsIgnoreCase("levelingcore_level")) {
            return String.valueOf(levelService.getLevel(playerRef.getUuid()));
        }
        if (params.equalsIgnoreCase("levelingcore_xp")) {
            return String.valueOf(levelService.getXp(playerRef.getUuid()));
        }
        if (params.equalsIgnoreCase("player_name")) {
            return playerRef.getUsername();
        }
        return null;
    }
}
