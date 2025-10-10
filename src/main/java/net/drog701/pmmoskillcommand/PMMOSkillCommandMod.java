package net.drog701.pmmoskillcommand;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.fml.loading.FMLLoader; // Use FMLLoader to check environment

@Mod("pmmoskillcommand")
public class PMMOSkillCommandMod {
    public PMMOSkillCommandMod() {
        NeoForge.EVENT_BUS.register(new PMMOSkillCommandTickHandler());

        // Only register the reload listener on the client
        if (FMLLoader.getDist().isClient()) {
            NeoForge.EVENT_BUS.addListener(this::onAddReloadListener);
        }
    }

    private void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(ToastConfigLoader.RELOAD_LISTENER);
    }
}