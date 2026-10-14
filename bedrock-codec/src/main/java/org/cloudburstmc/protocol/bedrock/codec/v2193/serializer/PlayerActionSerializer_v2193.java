package org.cloudburstmc.protocol.bedrock.codec.v2193.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.data.PlayerActionType;
import org.cloudburstmc.protocol.bedrock.packet.PlayerActionPacket;
import org.cloudburstmc.protocol.bedrock.util.VarInts;

import java.util.EnumMap;
import java.util.Map;

/**
 * Minecraft Bedrock 2193 PlayerActionType wire IDs.
 *
 * <p>The legacy shared enum contains removed placeholders and therefore
 * its ordinal() cannot be used to encode the protocol ID.</p>
 */
public final class PlayerActionSerializer_v2193 implements BedrockPacketSerializer<PlayerActionPacket> {
    public static final PlayerActionSerializer_v2193 INSTANCE = new PlayerActionSerializer_v2193();

    // Exact values from Mojang 2193 schema. Preserve the shared enum ordinal
    // positions because other older codecs still consume them.
    private static final PlayerActionType[] BY_ID = {
            PlayerActionType.UNKNOWN, // 0
            PlayerActionType.START_BREAK, // 1
            PlayerActionType.ABORT_BREAK, // 2
            PlayerActionType.STOP_BREAK, // 3
            PlayerActionType.START_SLEEP, // 4
            PlayerActionType.STOP_SLEEP, // 5
            PlayerActionType.RESPAWN, // 6
            PlayerActionType.JUMP, // 7
            PlayerActionType.START_SPRINT, // 8
            PlayerActionType.STOP_SPRINT, // 9
            PlayerActionType.START_SNEAK, // 10
            PlayerActionType.STOP_SNEAK, // 11
            PlayerActionType.DIMENSION_CHANGE_REQUEST_OR_CREATIVE_DESTROY_BLOCK, // 12
            PlayerActionType.DIMENSION_CHANGE_SUCCESS, // 13
            PlayerActionType.START_GLIDE, // 14
            PlayerActionType.STOP_GLIDE, // 15
            PlayerActionType.BUILD_DENIED, // 16
            PlayerActionType.CONTINUE_BREAK, // 17 (CrackBlock)
            PlayerActionType.START_SWIMMING, // 18
            PlayerActionType.STOP_SWIMMING, // 19
            PlayerActionType.START_SPIN_ATTACK, // 20
            PlayerActionType.STOP_SPIN_ATTACK, // 21
            PlayerActionType.BLOCK_PREDICT_DESTROY, // 22
            PlayerActionType.BLOCK_CONTINUE_DESTROY, // 23
            PlayerActionType.START_ITEM_USE_ON, // 24
            PlayerActionType.STOP_ITEM_USE_ON, // 25
            PlayerActionType.HANDLED_TELEPORT, // 26
            PlayerActionType.MISSED_SWING, // 27
            PlayerActionType.START_CRAWLING, // 28
            PlayerActionType.STOP_CRAWLING, // 29
            PlayerActionType.START_FLYING, // 30
            PlayerActionType.STOP_FLYING, // 31
            PlayerActionType.START_USING_ITEM, // 32
            PlayerActionType.INTERNAL_UPDATE, // 33
            PlayerActionType.COUNT // 34
    };
    private static final Map<PlayerActionType, Integer> BY_TYPE = createReverseMap();

    private PlayerActionSerializer_v2193() {
    }

    private static Map<PlayerActionType, Integer> createReverseMap() {
        Map<PlayerActionType, Integer> map = new EnumMap<>(PlayerActionType.class);
        for (int i = 0; i < BY_ID.length; i++) {
            if (map.put(BY_ID[i], i) != null) {
                throw new IllegalStateException("Duplicate 2193 action mapping: " + BY_ID[i]);
            }
        }
        return map;
    }

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, PlayerActionPacket packet) {
        Integer id = BY_TYPE.get(packet.getAction());
        if (id == null) {
            throw new IllegalArgumentException("Action not defined by 2193: " + packet.getAction());
        }
        VarInts.writeUnsignedLong(buffer, packet.getRuntimeEntityId());
        VarInts.writeInt(buffer, id);
        helper.writeBlockPosition(buffer, packet.getBlockPosition());
        helper.writeBlockPosition(buffer, packet.getResultPosition());
        VarInts.writeInt(buffer, packet.getFace());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, PlayerActionPacket packet) {
        packet.setRuntimeEntityId(VarInts.readUnsignedLong(buffer));
        int id = VarInts.readInt(buffer);
        if (id < 0 || id >= BY_ID.length) {
            throw new IllegalArgumentException("Invalid 2193 player action: " + id);
        }
        packet.setAction(BY_ID[id]);
        packet.setBlockPosition(helper.readBlockPosition(buffer));
        packet.setResultPosition(helper.readBlockPosition(buffer));
        packet.setFace(VarInts.readInt(buffer));
    }
}
