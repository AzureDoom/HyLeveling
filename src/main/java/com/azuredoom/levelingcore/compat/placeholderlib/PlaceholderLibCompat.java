package com.azuredoom.levelingcore.compat.placeholderlib;

import com.snoxium.placeholderlib.PlaceholderLib;
import com.snoxium.placeholderlib.api.SimplePlaceholder;

import com.azuredoom.levelingcore.api.LevelingCoreApi;

public class PlaceholderLibCompat {

    private PlaceholderLibCompat() {}

    public static void register() {
        var levelService = LevelingCoreApi.getLevelServiceIfPresent().orElse(null);
        if (levelService == null)
            return;
        PlaceholderLib.getRegistry()
            .register(
                new SimplePlaceholder(
                    "levelingcore_level",
                    (ctx -> String.valueOf(
                        levelService.getLevel(ctx.getPlayerRef().getUuid())
                    ))
                )
            );
        PlaceholderLib.getRegistry()
            .register(
                new SimplePlaceholder(
                    "levelingcore_xp",
                    (ctx -> String.valueOf(
                        levelService.getXp(ctx.getPlayerRef().getUuid())
                    ))
                )
            );
    }
}
