package com.azuredoom.levelingcore.systems.nameplate;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import com.azuredoom.levelingcore.LevelingCore;
import com.azuredoom.levelingcore.api.LevelingCoreApi;
import com.azuredoom.levelingcore.config.GUIConfig;
import com.azuredoom.levelingcore.utils.MobLevelingUtil;

public class ShowLvlHeadSystem implements Runnable {

    private final Config<GUIConfig> config;

    public ShowLvlHeadSystem(Config<GUIConfig> config) {
        this.config = config;
    }

    @Override
    public void run() {
        var universe = Universe.get();
        if (universe != null) {
            for (var world : universe.getWorlds().values()) {
                if (world != null && world.isAlive()) {
                    world.execute(() -> tickWorld(world));
                }
            }
        }
    }

    private void tickWorld(World world) {
        var store = world.getEntityStore().getStore();

        if (store == null)
            return;

        if (config.get().isShowPlayerLvls()) {
            var levelingServiceOpt = LevelingCoreApi.getLevelServiceIfPresent();
            if (levelingServiceOpt.isEmpty())
                return;

            var levelingService = levelingServiceOpt.get();

            store.forEachChunk(PlayerRef.getComponentType(), (chunk, commandBuffer) -> {
                var size = chunk.size();
                for (var i = 0; i < size; i++) {
                    var ref = chunk.getReferenceTo(i);

                    var playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
                    if (playerRef == null)
                        continue;

                    var lvl = levelingService.getLevel(playerRef.getUuid());
                    upsertNameplate(commandBuffer, ref, formatNameplate(playerRef.getUsername(), lvl));
                }
            });
        }

        if (config.get().isShowMobLvls()) {
            store.forEachChunk(NPCEntity.getComponentType(), (chunk, commandBuffer) -> {
                var size = chunk.size();
                for (var i = 0; i < size; i++) {
                    var ref = chunk.getReferenceTo(i);

                    var npc = commandBuffer.getComponent(ref, NPCEntity.getComponentType());
                    if (npc == null)
                        continue;
                    final var entityId = npc.getUuid();
                    var lvl = LevelingCore.mobLevelRegistry.getOrCreateWithPersistence(
                        entityId,
                        () -> MobLevelingUtil.computeSpawnLevel(npc),
                        0,
                        LevelingCore.mobLevelPersistence
                    );
                    if (lvl == null)
                        continue;

                    var text = formatNameplate(npc.getRoleName(), lvl.level);
                    upsertNameplate(commandBuffer, ref, text);
                }
            });
        }
    }

    private void upsertNameplate(
        CommandBuffer<EntityStore> commandBuffer,
        Ref<EntityStore> ref,
        String desiredText
    ) {
        if (desiredText == null || desiredText.isBlank()) {
            commandBuffer.removeComponent(ref, Nameplate.getComponentType());
            return;
        }

        var current = commandBuffer.getComponent(ref, Nameplate.getComponentType());
        if (current != null) {
            var old = current.getText();
            if (desiredText.equals(old)) {
                return;
            }
            current.setText(desiredText);
            commandBuffer.putComponent(ref, Nameplate.getComponentType(), current);
            return;
        }

        commandBuffer.putComponent(ref, Nameplate.getComponentType(), new Nameplate(desiredText));
    }

    private String formatNameplate(String name, int level) {
        if (name != null && !name.isBlank()) {
            return name + "'s Lvl " + level;
        }
        return "Lvl " + level;
    }
}
