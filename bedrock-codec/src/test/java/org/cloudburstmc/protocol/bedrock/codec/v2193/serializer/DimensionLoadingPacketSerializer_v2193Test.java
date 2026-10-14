package org.cloudburstmc.protocol.bedrock.codec.v2193.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.codec.v2169.Bedrock_v2169;
import org.cloudburstmc.protocol.bedrock.codec.v2193.Bedrock_v2193;
import org.cloudburstmc.protocol.bedrock.data.PlayerActionType;
import org.cloudburstmc.protocol.bedrock.data.ServerboundLoadingScreenPacketType;
import org.cloudburstmc.protocol.bedrock.packet.ChangeDimensionPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerActionPacket;
import org.cloudburstmc.protocol.bedrock.packet.ServerboundLoadingScreenPacket;
import org.cloudburstmc.protocol.bedrock.util.VarInts;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for the *actual* 1.26.52 client traffic.
 *
 * The initial experimental 2193 serializers incorrectly assumed Start=0,
 * End=1 and a required uint32 screen ID. The real client sends End=2 and
 * optional loading IDs. Those assumptions caused PacketSerializeException
 * and disconnected players with "Block" in the 2026-10-14 server.log.
 *
 * The known-working shared serializers must remain in use until an actual
 * capture supports a different version-specific wire format.
 */
class DimensionLoadingPacketSerializer_v2193Test {
    @Test
    void retainsKnownWorkingPacketSerializersFor2193() {
        assertSame(
                Bedrock_v2169.CODEC.getPacketDefinition(ChangeDimensionPacket.class).serializer(),
                Bedrock_v2193.CODEC.getPacketDefinition(ChangeDimensionPacket.class).serializer());
        assertSame(
                Bedrock_v2169.CODEC.getPacketDefinition(ServerboundLoadingScreenPacket.class).serializer(),
                Bedrock_v2193.CODEC.getPacketDefinition(ServerboundLoadingScreenPacket.class).serializer());
        assertSame(
                Bedrock_v2169.CODEC.getPacketDefinition(PlayerActionPacket.class).serializer(),
                Bedrock_v2193.CODEC.getPacketDefinition(PlayerActionPacket.class).serializer());
    }

    @Test
    void acceptsClientEndLoadingScreenEventTwoWithAbsentOptionalId() {
        var serializer = Bedrock_v2193.CODEC
                .getPacketDefinition(ServerboundLoadingScreenPacket.class).serializer();
        var helper = Bedrock_v2193.CODEC.createHelper();

        var packet = new ServerboundLoadingScreenPacket();
        packet.setType(ServerboundLoadingScreenPacketType.END_LOADING_SCREEN);
        packet.setLoadingScreenId(null);
        ByteBuf wire = Unpooled.buffer();
        try {
            serializer.serialize(wire, helper, packet);
            assertEquals(2, VarInts.readInt(wire), "Actual 2193 client uses End=2");
            assertFalse(wire.readBoolean(), "A missing screen ID is an optional-absent value");
            assertFalse(wire.isReadable());
            wire.readerIndex(0);
            var decoded = new ServerboundLoadingScreenPacket();
            serializer.deserialize(wire, helper, decoded);
            assertEquals(packet, decoded);
            assertFalse(wire.isReadable());
        } finally {
            wire.release();
        }
    }

    @Test
    void acceptsStartEventOneAndNonNullScreenId() {
        var serializer = Bedrock_v2193.CODEC
                .getPacketDefinition(ServerboundLoadingScreenPacket.class).serializer();
        var helper = Bedrock_v2193.CODEC.createHelper();
        var packet = new ServerboundLoadingScreenPacket();
        packet.setType(ServerboundLoadingScreenPacketType.START_LOADING_SCREEN);
        packet.setLoadingScreenId(123);
        ByteBuf wire = Unpooled.buffer();
        try {
            serializer.serialize(wire, helper, packet);
            assertEquals(1, VarInts.readInt(wire));
            assertTrue(wire.readBoolean());
            assertEquals(123, wire.readIntLE());
            assertFalse(wire.isReadable());
            wire.readerIndex(0);
            var decoded = new ServerboundLoadingScreenPacket();
            serializer.deserialize(wire, helper, decoded);
            assertEquals(packet, decoded);
        } finally {
            wire.release();
        }
    }

    @Test
    void dimensionChangeKeepsOptionalScreenIdFraming() {
        var serializer = Bedrock_v2193.CODEC
                .getPacketDefinition(ChangeDimensionPacket.class).serializer();
        var helper = Bedrock_v2193.CODEC.createHelper();
        var packet = new ChangeDimensionPacket();
        packet.setDimension(1);
        packet.setPosition(Vector3f.from(2, 83.62f, -3));
        packet.setRespawn(false);
        packet.setLoadingScreenId(1);
        ByteBuf wire = Unpooled.buffer();
        try {
            serializer.serialize(wire, helper, packet);
            assertEquals(1, VarInts.readInt(wire));
            helper.readVector3f(wire);
            assertFalse(wire.readBoolean());
            assertTrue(wire.readBoolean(), "Optional-present flag must precede ID");
            assertEquals(1, wire.readIntLE());
            assertFalse(wire.isReadable());
            wire.readerIndex(0);
            var decoded = new ChangeDimensionPacket();
            serializer.deserialize(wire, helper, decoded);
            assertEquals(packet, decoded);
        } finally {
            wire.release();
        }
    }

    @Test
    void dimensionAcknowledgementRetainsKnownWorkingLegacyActionWireId() {
        var serializer = Bedrock_v2193.CODEC
                .getPacketDefinition(PlayerActionPacket.class).serializer();
        var helper = Bedrock_v2193.CODEC.createHelper();
        var packet = new PlayerActionPacket();
        packet.setRuntimeEntityId(1);
        packet.setBlockPosition(Vector3i.ZERO);
        packet.setResultPosition(Vector3i.ZERO);
        packet.setAction(PlayerActionType.DIMENSION_CHANGE_SUCCESS);
        ByteBuf wire = Unpooled.buffer();
        try {
            serializer.serialize(wire, helper, packet);
            assertEquals(1L, VarInts.readUnsignedLong(wire));
            assertEquals(PlayerActionType.DIMENSION_CHANGE_SUCCESS.ordinal(), VarInts.readInt(wire));
            wire.readerIndex(0);
            var decoded = new PlayerActionPacket();
            serializer.deserialize(wire, helper, decoded);
            assertEquals(packet, decoded);
        } finally {
            wire.release();
        }
    }
}
