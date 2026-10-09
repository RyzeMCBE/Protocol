package org.cloudburstmc.protocol.bedrock.codec.v2168.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.codec.v2168.Bedrock_v2168;
import org.cloudburstmc.protocol.bedrock.data.ClientPlayMode;
import org.cloudburstmc.protocol.bedrock.data.InputInteractionModel;
import org.cloudburstmc.protocol.bedrock.data.InputMode;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerAuthInputSerializer_v2168Test {

    @Test
    void idlePacketUsesTheV2168PresenceFraming() {
        var helper = Bedrock_v2168.CODEC.createHelper();
        var packet = idlePacket();
        ByteBuf buffer = Unpooled.buffer();

        PlayerAuthInputSerializer_v2168.INSTANCE.serialize(buffer, helper, packet);

        // Rotation, position and motion occupy the first 32 bytes. The next
        // byte is the required presence marker, followed by the zero flag count.
        assertEquals(1, buffer.getUnsignedByte(32));
        assertEquals(0, buffer.getUnsignedByte(33));
        assertEquals(96, buffer.readableBytes());

        var decoded = new PlayerAuthInputPacket();
        PlayerAuthInputSerializer_v2168.INSTANCE.deserialize(buffer, helper, decoded);

        assertEquals(packet.getPosition(), decoded.getPosition());
        assertEquals(packet.getRotation(), decoded.getRotation());
        assertEquals(packet.getInputMode(), decoded.getInputMode());
        assertEquals(packet.getPlayMode(), decoded.getPlayMode());
        assertEquals(packet.getInputInteractionModel(), decoded.getInputInteractionModel());
        assertTrue(decoded.getInputData().isEmpty());
        assertEquals(0, buffer.readableBytes());
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
