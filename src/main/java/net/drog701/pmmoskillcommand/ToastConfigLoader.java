package net.drog701.pmmoskillcommand;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStreamReader;

public class ToastConfigLoader {
    private static ToastConfig config;

    public static ToastConfig getConfig() {
        if (config != null) return config;
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        ResourceLocation loc = ResourceLocation.tryParse("pmmoskillcommand:toast_config.json");
        try {
            for (Resource resource : manager.getResourceStack(loc)) {
                try (InputStreamReader reader = new InputStreamReader(resource.open())) {
                    config = new Gson().fromJson(reader, ToastConfig.class);
                    if (config != null) break;
                }
            }
        } catch (Exception e) {
            // Ignore, use default config below
        }
        if (config == null) config = new ToastConfig();
        return config;
    }
}