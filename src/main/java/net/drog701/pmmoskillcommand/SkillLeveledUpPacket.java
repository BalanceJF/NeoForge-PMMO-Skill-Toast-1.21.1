package net.drog701.pmmoskillcommand;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;

public record SkillLeveledUpPacket(String skill, int level) implements CustomPacketPayload {
    public static final Type<SkillLeveledUpPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("pmmoskillcommand", "skill_leveled_up"));

    public static final StreamCodec<FriendlyByteBuf, SkillLeveledUpPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public SkillLeveledUpPacket decode(FriendlyByteBuf buf) {
                    String skill = buf.readUtf();
                    int level = buf.readInt();
                    return new SkillLeveledUpPacket(skill, level);
                }
                @Override
                public void encode(FriendlyByteBuf buf, SkillLeveledUpPacket pkt) {
                    buf.writeUtf(pkt.skill());
                    buf.writeInt(pkt.level());
                }
            };

    @Override
    public Type<SkillLeveledUpPacket> type() {
        return TYPE;
    }
}