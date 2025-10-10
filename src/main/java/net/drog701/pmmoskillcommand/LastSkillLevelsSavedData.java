package net.drog701.pmmoskillcommand;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class LastSkillLevelsSavedData extends SavedData {
    private static final String DATA_NAME = "pmmoskillcommand_last_skill_levels";
    private static final Gson GSON = new Gson();
    private final Map<String, Map<String, Integer>> playerSkillLevels = new HashMap();
    public static final SavedData.Factory<LastSkillLevelsSavedData> FACTORY = new SavedData.Factory(LastSkillLevelsSavedData::new, (nbtTag, provider) -> {
        LastSkillLevelsSavedData data = new LastSkillLevelsSavedData();
        if (nbtTag.contains("playerSkillLevels")) {
            Type type = (new TypeToken<Map<String, Map<String, Integer>>>() {
            }).getType();
            Map<String, Map<String, Integer>> loaded = (Map)GSON.fromJson(nbtTag.getString("playerSkillLevels"), type);
            if (loaded != null) {
                data.playerSkillLevels.putAll(loaded);
            }
        }

        return data;
    });

    public static LastSkillLevelsSavedData get(ServerLevel level) {
        return (LastSkillLevelsSavedData)level.getDataStorage().computeIfAbsent(FACTORY, "pmmoskillcommand_last_skill_levels");
    }

    public Map<String, Map<String, Integer>> getPlayerSkillLevels() {
        return this.playerSkillLevels;
    }

    public void setPlayerSkillLevels(String uuid, Map<String, Integer> levels) {
        this.playerSkillLevels.put(uuid, new HashMap(levels));
        this.setDirty();
    }

    public Map<String, Integer> getLevelsForPlayer(String uuid) {
        return (Map)this.playerSkillLevels.get(uuid);
    }

    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        String json = GSON.toJson(this.playerSkillLevels);
        tag.putString("playerSkillLevels", json);
        return tag;
    }
}