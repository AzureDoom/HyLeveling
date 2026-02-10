package com.azuredoom.levelingcore.utils;

import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.NotificationUtil;

import com.azuredoom.levelingcore.lang.CommandLang;

/**
 * Utility class for sending notifications to players. Provides a method to send success notifications with a given
 * message to a specified player reference.
 */
public class NotificationsUtil {

    private NotificationsUtil() {}

    /**
     * Sends a success notification to a specific player.
     *
     * @param playerRef The reference object representing the target player.
     * @param message   The message to be displayed in the notification.
     */
    public static void sendNotification(PlayerRef playerRef, Message message) {
        NotificationUtil.sendNotification(
            playerRef.getPacketHandler(),
            message,
            NotificationStyle.Success
        );
    }

    public static void sendXPGainNotification(PlayerRef playerRef, long xpAmount) {
        sendNotification(
            playerRef,
            CommandLang.GAINED.param("xp", xpAmount)
        );
    }
}
