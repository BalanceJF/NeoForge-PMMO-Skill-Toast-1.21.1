package net.drog701.pmmoskillcommand;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = "pmmoskillcommand", bus = Bus.MOD)
public class PMMONetworkRegistration {
    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        System.out.println("RegisterPayloadHandlersEvent called!");
        event.registrar("PLAY")
                .playToClient(
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