package org.cloudburstmc.protocol.bedrock.codec.v2193.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.codec.v2193.Bedrock_v2193;
import org.cloudburstmc.protocol.bedrock.data.ClientPlayMode;
import org.cloudburstmc.protocol.bedrock.data.InputInteractionModel;
import org.cloudburstmc.protocol.bedrock.data.InputMode;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerAuthInputSerializer_v2193Test {

    @Test
    void idlePacketUsesTheV2193PresenceFraming() {
        var helper = Bedrock_v2193.CODEC.createHelper();
        var packet = idlePacket();
        ByteBuf buffer = Unpooled.buffer();

        try {
            PlayerAuthInputSerializer_v2193.INSTANCE.serialize(buffer, helper, packet);

            // 2193 removed the v2168 outer marker. Byte 32 is now the input flag count.
            assertEquals(0, buffer.getUnsignedByte(32));
            assertEquals(92, buffer.readableBytes());

            var decoded = new PlayerAuthInputPacket();
            PlayerAuthInputSerializer_v2193.INSTANCE.deserialize(buffer, helper, decoded);

            assertEquals(packet.getPosition(), decoded.getPosition());
            assertEquals(packet.getRotation(), decoded.getRotation());
            assertEquals(packet.getInputMode(), decoded.getInputMode());
            assertEquals(packet.getPlayMode(), decoded.getPlayMode());
            assertEquals(packet.getInputInteractionModel(), decoded.getInputInteractionModel());
            assertTrue(decoded.getInputData().isEmpty());
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    private static PlayerAuthInputPacket idlePacket() {
        var packet = new PlayerAuthInputPacket();
        packet.setRotation(Vector3f.ZERO);
        packet.setPosition(Vector3f.from(12.5f, 70.62f, -4.25f));
        packet.setMotion(Vector2f.from(0, 0));
        packet.setInputMode(InputMode.TOUCH);
        packet.setPlayMode(ClientPlayMode.NORMAL);
        packet.setInputInteractionModel(InputInteractionModel.TOUCH);
        packet.setInteractRotation(Vector2f.from(0, 0));
        packet.setTick(0);
        packet.setDelta(Vector3f.ZERO);
        packet.setAnalogMoveVector(Vector2f.from(0, 0));
        packet.setCameraOrientation(Vector3f.ZERO);
        packet.setRawMoveVector(Vector2f.from(0, 0));
        return packet;
    }
}
