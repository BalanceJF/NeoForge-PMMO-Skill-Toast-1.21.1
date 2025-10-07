package net.drog701.pmmoskillcommand;

import harmonised.pmmo.api.APIUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

@Mod("pmmoskillcommand")
public class PMMOSkillCommandMod {

    // Map: <Player UUID, Map<Skill, Level>>
    private static final Map<String, Map<String, Integer>> playerSkillLevels = new HashMap<>();

    // List of skills you want to monitor (update as needed)
    private static final String[] SKILLS = {
            "mining", "combat", "farming", "fishing", "woodcutting", "excavation", "archery", "smithing"
            // Add other skill names here as desired
    };

    public PMMOSkillCommandMod() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player.level().isClientSide) return; // Only run on server side
        if (event.phase != TickEvent.Phase.END) return; // Only once per tick

        // Only check once per second for efficiency
        if (event.player.tickCount % 20 != 0) return;

        ServerPlayer player = (ServerPlayer) event.player;
        String uuid = player.getStringUUID();
        Map<String, Integer> lastLevels = playerSkillLevels.computeIfAbsent(uuid, k -> new HashMap<>());

        for (String skill : SKILLS) {
            int currentLevel = APIUtils.getPlayerSkillLevel(player, skill);
            int lastLevel = lastLevels.getOrDefault(skill, 0);

            if (currentLevel > lastLevel) {
                // Skill leveled up!
                player.sendSystemMessage(Component.literal(
                        String.format("§6%s§r leveled up! New level: §e%d", capitalize(skill), currentLevel)
                ));
                // You can also run commands, display titles, etc. here
            }
            lastLevels.put(skill, currentLevel);
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}