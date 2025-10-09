package net.drog701.pmmoskillcommand;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class LastSkillLevelsSavedData extends SavedData {
    private static final String DATA_NAME = "pmmoskillcommand_last_skill_levels";
    private static final Gson GSON = new Gson();

    // UUID -> skill -> level
    private final Map<String, Map<String, Integer>> playerSkillLevels = new HashMap<>();

    public static final SavedData.Factory<LastSkillLevelsSavedData> FACTORY =
            new SavedData.Factory<>(
                    LastSkillLevelsSavedData::new,
                    (CompoundTag nbtTag, HolderLookup.Provider provider) -> {
                        LastSkillLevelsSavedData data = new LastSkillLevelsSavedData();
                        if (nbtTag.contains("playerSkillLevels")) {
                            Type type = new TypeToken<Map<String, Map<String, Integer>>>() {}.getType();
                            Map<String, Map<String, Integer>> loaded =
                                    GSON.fromJson(nbtTag.getString("playerSkillLevels"), type);
                            if (loaded != null) data.playerSkillLevels.putAll(loaded);
                        }
                        return data;
                    }
            );

    public static LastSkillLevelsSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public Map<String, Map<String, Integer>> getPlayerSkillLevels() {
        return playerSkillLevels;
    }

    public void setPlayerSkillLevels(String uuid, Map<String, Integer> levels) {
        playerSkillLevels.put(uuid, new HashMap<>(levels));
        setDirty();
    }

    public Map<String, Integer> getLevelsForPlayer(String uuid) {
        return playerSkillLevels.get(uuid);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        String json = GSON.toJson(playerSkillLevels);
        tag.putString("playerSkillLevels", json);
        return tag;
    }
}