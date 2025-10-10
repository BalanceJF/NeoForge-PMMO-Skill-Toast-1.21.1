package net.drog701.pmmoskillcommand;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ToastConfigLoader {
    private static volatile ToastConfig config = new ToastConfig(); // always non-null

    /**
     * Called by our reload listener to reload the config from highest-priority resource pack.
     * Never access Minecraft.getInstance() or registries here!
     */
    public static void reload(ResourceManager manager) {
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("pmmoskillcommand", "toast_config.json");
        try {
            Optional<Resource> optional = manager.getResource(loc);
            if (optional.isPresent()) {
                try (InputStreamReader reader = new InputStreamReader(optional.get().open())) {
                    ToastConfig loaded = new Gson().fromJson(reader, ToastConfig.class);
                    if (loaded != null) {
                        config = loaded;
                        System.out.println("[PMMOSkillCommand] Loaded toast config: texture=" + config.texture + ", sound=" + config.sound);
                    } else {
                        config = new ToastConfig();
                        System.out.println("[PMMOSkillCommand] toast_config.json invalid (null), using defaults.");
                    }
                }
            } else {
                config = new ToastConfig();
                System.out.println("[PMMOSkillCommand] toast_config.json not found, using defaults.");
            }
        } catch (Exception e) {
            config = new ToastConfig();
            System.out.println("[PMMOSkillCommand] Failed to load toast_config.json, using defaults. Error: " + e);
        }
    }

    /**
     * Access the current config (never null).
     */
    public static ToastConfig getConfig() {
        return config;
    }

    /**
     * The reload listener for resource reloading.
     */
    public static final PreparableReloadListener RELOAD_LISTENER = new PreparableReloadListener() {
        @Override
        public CompletableFuture<Void> reload(
                PreparationBarrier stage,
                ResourceManager resourceManager,
                ProfilerFiller preparationsProfiler,
                ProfilerFiller reloadProfiler,
                Executor backgroundExecutor,
                Executor gameExecutor
        ) {
            // Fully qualify static method call to avoid ambiguity with overridden method
            return CompletableFuture.runAsync(() -> ToastConfigLoader.reload(resourceManager), backgroundExecutor);
        }
    };
}