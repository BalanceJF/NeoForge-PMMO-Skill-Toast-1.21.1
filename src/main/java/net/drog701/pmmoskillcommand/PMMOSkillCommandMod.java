package net.drog701.pmmoskillcommand;

import harmonised.pmmo.api.APIUtils;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import java.util.HashMap;
import java.util.Map;

@Mod("pmmoskillcommand")
public class PMMOSkillCommandMod {

    private static final Map<String, Map<String, Integer>> playerSkillLevels = new HashMap<>();
    private final Map<ServerPlayer, Integer> scheduledRevokeTicks = new HashMap<>();

    private static final String[] SKILLS = {
            "mining", "combat", "farming", "fishing", "woodcutting",
            "excavation", "archery", "smithing", "agility"
    };

    public PMMOSkillCommandMod() {
        NeoForge.EVENT_BUS.register(this); // For instance @SubscribeEvent methods (like tick)
        PMMONetworkRegistration.init(); // Ensure static modbus event handler is loaded
    }

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        for (ServerPlayer player : level.players()) {
            if (player.tickCount % 20 != 0) continue;

            String uuid = player.getStringUUID();
            Map<String, Integer> lastLevels = playerSkillLevels.computeIfAbsent(uuid, k -> new HashMap<>());

            for (String skill : SKILLS) {
                long currentLevel = APIUtils.getLevel(skill, player);
                int lastLevel = lastLevels.getOrDefault(skill, 0);

                System.out.println("Skill: " + skill + ", currentLevel: " + currentLevel + ", lastLevel: " + lastLevel);

                if (currentLevel > lastLevel) {
                    ResourceLocation advId = ResourceLocation.fromNamespaceAndPath("pmmoskillcommand", "skill_toast");
                    AdvancementHolder adv = player.server.getAdvancements().get(advId);
                    if (adv != null) {
                        System.out.println("AdvancementHolder found, awarding...");
                        player.getAdvancements().award(adv, "trigger");
                        scheduledRevokeTicks.put(player, 100);
                    } else {
                        System.out.println("AdvancementHolder is null for: " + advId);
                    }

                    // Send the toast packet to the client
                    player.connection.send(new SkillLeveledUpPacket(skill, (int) currentLevel));
                }
                lastLevels.put(skill, (int) currentLevel);
            }
        }

        // Process scheduled revokes
        scheduledRevokeTicks.entrySet().removeIf(entry -> {
            int ticksLeft = entry.getValue() - 1;
            if (ticksLeft <= 0) {
                ServerPlayer p = entry.getKey();
                ResourceLocation advId = ResourceLocation.fromNamespaceAndPath("pmmoskillcommand", "skill_toast");
                AdvancementHolder toRevokeAdv = p.server.getAdvancements().get(advId);
                if (toRevokeAdv != null) {
                    p.getAdvancements().revoke(toRevokeAdv, "trigger");
                }
                return true;
            } else {
                entry.setValue(ticksLeft);
                return false;
            }
        });
    }
}