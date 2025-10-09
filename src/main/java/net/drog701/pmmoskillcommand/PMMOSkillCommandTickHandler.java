package net.drog701.pmmoskillcommand;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import harmonised.pmmo.api.APIUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PMMOSkillCommandTickHandler {

    // Dynamically loaded set of tracked skills
    private static Set<String> trackedSkills = new HashSet<>();
    private static Path lastSkillsFilePath = null;
    private static int skillsJsonTickCounter = 0;

    // Update trackedSkills set from the current world's skills.json once every 20 ticks
    private static void updateSkillsList(ServerLevel level) {
        MinecraftServer server = level.getServer();
        Path skillsFile = server.getWorldPath(LevelResource.ROOT)
                .resolve("datapacks/generated_pack/data/pmmo/config/skills.json");

        // Only reload if file changed or set is empty
        if (skillsFile.equals(lastSkillsFilePath) && !trackedSkills.isEmpty()) return;
        lastSkillsFilePath = skillsFile;

        Set<String> newSkills = new HashSet<>();
        try {
            if (!Files.exists(skillsFile)) {
                System.out.println("[PMMO Skill Command] skills.json not found at: " + skillsFile);
                trackedSkills = newSkills;
                return;
            }
            String json = Files.readString(skillsFile);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject skillsObject = root.getAsJsonObject("skills");
            for (String skill : skillsObject.keySet()) {
                newSkills.add(skill);
            }
            trackedSkills = newSkills;
            System.out.println("[PMMO Skill Command] Loaded skills: " + trackedSkills);
        } catch (IOException | JsonSyntaxException | IllegalStateException e) {
            System.err.println("[PMMO Skill Command] Error reading skills.json: " + e);
            trackedSkills = newSkills;
        }
    }

    // On player login, initialize their last-seen levels to current values and persist them, but do NOT send toasts
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();
        LastSkillLevelsSavedData data = LastSkillLevelsSavedData.get(level);

        String uuid = player.getStringUUID();
        Map<String, Integer> levels = new HashMap<>();
        for (String skill : trackedSkills) {
            levels.put(skill, (int) APIUtils.getLevel(skill, player));
        }
        data.setPlayerSkillLevels(uuid, levels);
        // Just storing, no toasts on login!
    }

    // On server tick, check for level increases and send toast packets for true increases only
    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        // Only check for new/updated skills.json once per 20 ticks
        skillsJsonTickCounter = (skillsJsonTickCounter + 1) % 20;
        if (skillsJsonTickCounter == 0) {
            updateSkillsList(level);
        }

        LastSkillLevelsSavedData data = LastSkillLevelsSavedData.get(level);

        for (ServerPlayer player : level.players()) {
            if (player.tickCount % 20 != 0) continue;

            String uuid = player.getStringUUID();

            // Loads last known levels from persistent storage, or initializes if missing
            Map<String, Integer> lastLevels = data.getLevelsForPlayer(uuid);
            if (lastLevels == null) {
                lastLevels = new HashMap<>();
                for (String skill : trackedSkills)
                    lastLevels.put(skill, (int) APIUtils.getLevel(skill, player));
                data.setPlayerSkillLevels(uuid, lastLevels);
                continue; // Don't show toasts on first load
            }

            boolean updated = false;

            for (String skill : trackedSkills) {
                long currentLevel = APIUtils.getLevel(skill, player);
                int lastLevel = lastLevels.getOrDefault(skill, 0);

                if (currentLevel > lastLevel) {
                    player.connection.send(new SkillLeveledUpPacket(skill, (int) currentLevel));
                    updated = true;
                }
                lastLevels.put(skill, (int) currentLevel);
            }

            if (updated) {
                data.setPlayerSkillLevels(uuid, lastLevels); // persist updated levels
            }
        }
    }
}