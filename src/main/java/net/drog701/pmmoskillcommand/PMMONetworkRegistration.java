package net.drog701.pmmoskillcommand;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class PMMONetworkRegistration {
    public static void init() {} // called just to force class load

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        event.registrar("PLAY").playToClient(
                SkillLeveledUpPacket.TYPE,
                SkillLeveledUpPacket.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        ToastHelper.showSkillToast(payload.skill(), payload.level());
                    });
                }
        );
    }
}