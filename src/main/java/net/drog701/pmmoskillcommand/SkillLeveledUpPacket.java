package net.drog701.pmmoskillcommand;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SkillLeveledUpPacket(String skill, int level) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SkillLeveledUpPacket> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("pmmoskillcommand", "skill_leveled_up"));
    public static final StreamCodec<FriendlyByteBuf, SkillLeveledUpPacket> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, SkillLeveledUpPacket>() {
        public SkillLeveledUpPacket decode(FriendlyByteBuf buf) {
            String skill = buf.readUtf();
            int level = buf.readInt();
            return new SkillLeveledUpPacket(skill, level);
        }

        public void encode(FriendlyByteBuf buf, SkillLeveledUpPacket pkt) {
            buf.writeUtf(pkt.skill());
            buf.writeInt(pkt.level());
        }
    };

    public CustomPacketPayload.Type<SkillLeveledUpPacket> type() {
        return TYPE;
    }
}