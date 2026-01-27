package com.azuredoom.levelingcore.utils;

import com.azuredoom.levelingcore.level.mobs.MobLevelData;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

public record PendingUpdate(
        NPCEntity npc,
        TransformComponent transform,
        Store<EntityStore> store,
        MobLevelData data
) {}
