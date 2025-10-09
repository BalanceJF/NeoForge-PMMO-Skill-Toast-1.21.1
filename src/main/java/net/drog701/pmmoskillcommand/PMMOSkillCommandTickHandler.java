package net.drog701.pmmoskillcommand;

import harmonised.pmmo.api.APIUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.Map;

public class PMMOSkillCommandTickHandler {

    // Tracks each player's last seen levels
    private static final Map<String, Map<String, Integer>> playerSkillLevels = new HashMap<>();

    // List your skills here
    private static final String[] SKILLS = {
            "mining", "combat", "farming", "fishing", "woodcutting",
            "excavation", "archery", "smithing", "agility"
    };

    // On player login, initialize their last-seen levels to current values
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        String uuid = player.getStringUUID();
        Map<String, Integer> levels = new HashMap<>();
        for (String skill : SKILLS) {
            levels.put(skill, (int) APIUtils.getLevel(skill, player));
        }
        playerSkillLevels.put(uuid, levels);
    }

    // On server tick, check for level increases and send toast packets
    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        for (ServerPlayer player : level.players()) {
            if (player.tickCount % 20 != 0) continue; // Only every second

            String uuid = player.getStringUUID();
            Map<String, Integer> lastLevels = playerSkillLevels.computeIfAbsent(uuid, k -> {
                // Defensive: If missing, initialize to current levels (shouldn't happen if login event works)
                Map<String, Integer> map = new HashMap<>();
                for (String skill : SKILLS)
                    map.put(skill, (int) APIUtils.getLevel(skill, player));
                return map;
            });

            for (String skill : SKILLS) {
                long currentLevel = APIUtils.getLevel(skill, player);
                int lastLevel = lastLevels.getOrDefault(skill, 0);

                if (currentLevel > lastLevel) {
                    // Only send the toast packet on actual increase
                    player.connection.send(new SkillLeveledUpPacket(skill, (int) currentLevel));
                }
                lastLevels.put(skill, (int) currentLevel);
            }
        }
    }
}