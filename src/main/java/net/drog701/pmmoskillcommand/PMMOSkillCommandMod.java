package net.drog701.pmmoskillcommand;

import harmonised.pmmo.api.APIUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;

@Mod("pmmoskillcommand")
public class PMMOSkillCommandMod {

    // Map: <Player UUID, Map<Skill, Level>>
    private static final Map<String, Map<String, Integer>> playerSkillLevels = new HashMap<>();

    // List of skills you want to monitor (update as needed)
    private static final String[] SKILLS = {
            "mining", "combat", "farming", "fishing", "woodcutting", "excavation", "archery", "smithing", "agility"
            // Add other skill names here as desired
    };

    public PMMOSkillCommandMod() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        // Only server side
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        for (ServerPlayer player : level.players()) {
            // Only check every 20 ticks for each player
            if (player.tickCount % 20 != 0) continue;

            String uuid = player.getStringUUID();
            Map<String, Integer> lastLevels = playerSkillLevels.computeIfAbsent(uuid, k -> new HashMap<>());

            for (String skill : SKILLS) {
                long currentLevel = APIUtils.getLevel(skill, player);
                int lastLevel = lastLevels.getOrDefault(skill, 0);

                if (currentLevel > lastLevel) {
                    // Skill leveled up!
                    player.sendSystemMessage(Component.literal(
                            String.format("§6%s§r §e%d", capitalize(skill), currentLevel)
                    ));
                    // You can also run commands, display titles, etc. here
                }
                lastLevels.put(skill, (int) currentLevel);
            }
        }
    }

    // This method is now outside of onLevelTick, but inside the class
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}