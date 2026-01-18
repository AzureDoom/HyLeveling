package com.azuredoom.levelingcore.compat;

import com.buuz135.mhud.MultipleHUD;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class MultipleHudCompat {

    private MultipleHudCompat() {}

    public static void showHud(Player player, PlayerRef playerRef, CustomUIHud xpHud) {
        MultipleHUD.getInstance().setCustomHud(player, playerRef, "levelingcore_xpbar", xpHud);
    }
}
