package com.azuredoom.levelingcore.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.EventTitleUtil;

import javax.annotation.Nonnull;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.api.LevelingCoreApi;

/**
 * The CheckLevelCommand class is a concrete implementation of the CommandBase class. This command allows users to check
 * the current level of a specified player in the game. It interacts with the Leveling Core API to retrieve level data
 * for a given player.
 */
public class CheckLevelCommand extends CommandBase {

    @Nonnull
    private final RequiredArg<PlayerRef> playerArg;

    public CheckLevelCommand() {
        super("checklevel", "Check level of player");
        this.playerArg = this.withRequiredArg(
            "player",
            "server.commands.levelingcore.checklevel.desc",
            ArgTypes.PLAYER_REF
        );
    }

    @Override
    protected void executeSync(@Nonnull CommandContext commandContext) {
        if (LevelingCoreApi.getLevelServiceIfPresent().isEmpty()) {
            commandContext.sendMessage(Message.raw("Leveling Core is not initialized"));
            return;
        }
        var playerRef = this.playerArg.get(commandContext);
        var playerUUID = playerRef.getUuid();
        var levelRef = LevelingCoreApi.getLevelServiceIfPresent().get().getLevel(playerUUID);
        var rankName = "Unranked";
        var rankManager = LevelingCore.getRankManager();
        if (rankManager != null) {
            rankName = rankManager.getRankForLevel(levelRef)
                .map(rank -> rank.name)
                .orElse("Unranked");
        }
        var currentLevelMsg = playerRef.getUsername() + " current level is " + levelRef + " [" + rankName + "]";
        EventTitleUtil.showEventTitleToPlayer(playerRef, Message.raw(currentLevelMsg), Message.raw(""), true);
        commandContext.sendMessage(Message.raw(currentLevelMsg));
    }
}
