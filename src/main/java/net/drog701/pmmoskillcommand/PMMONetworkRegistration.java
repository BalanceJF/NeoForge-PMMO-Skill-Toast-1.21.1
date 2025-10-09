package net.drog701.pmmoskillcommand;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class PMMONetworkRegistration {
    // Call this in the mod constructor to force class load
    public static void ensureLoaded() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        System.out.println("RegisterPayloadHandlersEvent called!");
        event.registrar("PLAY").playToClient(
                SkillLeveledUpPacket.TYPE,
                SkillLeveledUpPacket.STREAM_CODEC,
                (payload, ctx) -> {
                    ctx.enqueueWork(() -> {
                        ToastHelper.showSkillToast(payload.skill(), payload.level());
                    });
                }
        );
    }
}