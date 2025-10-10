package net.drog701.pmmoskillcommand;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import harmonised.pmmo.api.APIUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class PMMOSkillCommandTickHandler {
    private static Set<String> trackedSkills = new HashSet();
    private static Path lastSkillsFilePath = null;
    private static int skillsJsonTickCounter = 0;

    private static void updateSkillsList(ServerLevel level) {
        MinecraftServer server = level.getServer();
        Path skillsFile = server.getWorldPath(LevelResource.ROOT).resolve("datapacks/generated_pack/data/pmmo/config/skills.json");
        if (!skillsFile.equals(lastSkillsFilePath) || trackedSkills.isEmpty()) {
            lastSkillsFilePath = skillsFile;
            Set<String> newSkills = new HashSet();

            try {
                if (!Files.exists(skillsFile, new LinkOption[0])) {
                    System.out.println("[PMMO Skill Command] skills.json not found at: " + String.valueOf(skillsFile));
                    trackedSkills = newSkills;
                    return;
                }

                String json = Files.readString(skillsFile);
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                JsonObject skillsObject = root.getAsJsonObject("skills");

                for(String skill : skillsObject.keySet()) {
                    newSkills.add(skill);
                }

                trackedSkills = newSkills;
                System.out.println("[PMMO Skill Command] Loaded skills: " + String.valueOf(trackedSkills));
            } catch (JsonSyntaxException | IllegalStateException | IOException e) {
                System.err.println("[PMMO Skill Command] Error reading skills.json: " + String.valueOf(e));
                trackedSkills = newSkills;
            }

        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player var3 = event.getEntity();
        if (var3 instanceof ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            LastSkillLevelsSavedData data = LastSkillLevelsSavedData.get(level);
            String uuid = player.getStringUUID();
            HashMap levels = new HashMap();

            for(String skill : trackedSkills) {
                levels.put(skill, (int)APIUtils.getLevel(skill, player));
            }

            data.setPlayerSkillLevels(uuid, levels);
        }
    }

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        Level var3 = event.getLevel();
        if (var3 instanceof ServerLevel level) {
            skillsJsonTickCounter = (skillsJsonTickCounter + 1) % 20;
            if (skillsJsonTickCounter == 0) {
                updateSkillsList(level);
            }

            LastSkillLevelsSavedData data = LastSkillLevelsSavedData.get(level);

            for(ServerPlayer player : level.players()) {
                if (player.tickCount % 20 == 0) {
                    String uuid = player.getStringUUID();
                    Map<String, Integer> lastLevels = data.getLevelsForPlayer(uuid);
                    if (lastLevels == null) {
                        lastLevels = new HashMap();

                        for(String skill : trackedSkills) {
                            lastLevels.put(skill, (int)APIUtils.getLevel(skill, player));
                        }

                        data.setPlayerSkillLevels(uuid, lastLevels);
                    } else {
                        boolean updated = false;

                        for(String skill : trackedSkills) {
                            long currentLevel = APIUtils.getLevel(skill, player);
                            int lastLevel = (Integer)lastLevels.getOrDefault(skill, 0);
                            if (currentLevel > (long)lastLevel) {
                                player.connection.send(new SkillLeveledUpPacket(skill, (int)currentLevel));
                                updated = true;
                            }

                            lastLevels.put(skill, (int)currentLevel);
                        }

                        if (updated) {
                            data.setPlayerSkillLevels(uuid, lastLevels);
                        }
                    }
                }
            }

        }
    }
}