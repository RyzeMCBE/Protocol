package org.cloudburstmc.protocol.bedrock.codec.v2193.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.data.ServerboundLoadingScreenPacketType;
import org.cloudburstmc.protocol.bedrock.packet.ServerboundLoadingScreenPacket;
import org.cloudburstmc.protocol.bedrock.util.VarInts;

/**
 * 2193: StartLoadingScreen=0 and EndLoadingScreen=1; ID uses a required
 * uint32 LE field. Protocol 2207+ changed both enum values and ID presence.
 */
public final class ServerboundLoadingScreenSerializer_v2193
        implements BedrockPacketSerializer<ServerboundLoadingScreenPacket> {
    public static final ServerboundLoadingScreenSerializer_v2193 INSTANCE =
            new ServerboundLoadingScreenSerializer_v2193();

    private ServerboundLoadingScreenSerializer_v2193() {
    }

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ServerboundLoadingScreenPacket packet) {
        int type = switch (packet.getType()) {
            case START_LOADING_SCREEN -> 0;
            case END_LOADING_SCREEN -> 1;
            default -> throw new IllegalArgumentException("Unsupported 2193 loading screen event: " + packet.getType());
        };
        VarInts.writeInt(buffer, type);
        buffer.writeIntLE(packet.getLoadingScreenId() == null ? 0 : packet.getLoadingScreenId());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ServerboundLoadingScreenPacket packet) {
        int wireType = VarInts.readInt(buffer);
        packet.setType(switch (wireType) {
            case 0 -> ServerboundLoadingScreenPacketType.START_LOADING_SCREEN;
            case 1 -> ServerboundLoadingScreenPacketType.END_LOADING_SCREEN;
            default -> throw new IllegalArgumentException("Invalid 2193 loading screen event ID: " + wireType);
        });
        int id = buffer.readIntLE();
        packet.setLoadingScreenId(id == 0 ? null : id);
    }
}
