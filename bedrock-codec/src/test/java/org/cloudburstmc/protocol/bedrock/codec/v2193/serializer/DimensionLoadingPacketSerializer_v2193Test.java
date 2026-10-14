package org.cloudburstmc.protocol.bedrock.codec.v2193.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.codec.v2193.Bedrock_v2193;
import org.cloudburstmc.protocol.bedrock.data.PlayerActionType;
import org.cloudburstmc.protocol.bedrock.data.ServerboundLoadingScreenPacketType;
import org.cloudburstmc.protocol.bedrock.packet.ChangeDimensionPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerActionPacket;
import org.cloudburstmc.protocol.bedrock.packet.ServerboundLoadingScreenPacket;
import org.cloudburstmc.protocol.bedrock.util.VarInts;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DimensionLoadingPacketSerializer_v2193Test {
    @Test
    void usesTheNewSerializersOnlyOn2193() {
        assertSame(ChangeDimensionSerializer_v2193.INSTANCE,
                Bedrock_v2193.CODEC.getPacketDefinition(ChangeDimensionPacket.class).serializer());
        assertSame(ServerboundLoadingScreenSerializer_v2193.INSTANCE,
                Bedrock_v2193.CODEC.getPacketDefinition(ServerboundLoadingScreenPacket.class).serializer());
        assertSame(PlayerActionSerializer_v2193.INSTANCE,
                Bedrock_v2193.CODEC.getPacketDefinition(PlayerActionPacket.class).serializer());
    }

    @Test
    void changeDimensionWritesRequiredUint32WithoutOptionalPresenceMarker() {
        var helper = Bedrock_v2193.CODEC.createHelper();
        var source = new ChangeDimensionPacket();
        source.setDimension(1);
        source.setPosition(Vector3f.from(4.5f, 78.62f, -1.5f));
        source.setRespawn(false);
        source.setLoadingScreenId(0x12345678);
        ByteBuf buffer = Unpooled.buffer();
        try {
            ChangeDimensionSerializer_v2193.INSTANCE.serialize(buffer, helper, source);
            // Dimension varint (1 byte), position vec3 (12), respawn (1), raw LE32 ID (4).
            assertEquals(18, buffer.readableBytes());
            assertEquals(0x12345678, buffer.getIntLE(buffer.writerIndex() - 4));

            var decoded = new ChangeDimensionPacket();
            ChangeDimensionSerializer_v2193.INSTANCE.deserialize(buffer, helper, decoded);
            assertEquals(source, decoded);
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test
    void loadingScreenEventsHave2193IdsAndRequiredUint32() {
        var helper = Bedrock_v2193.CODEC.createHelper();
        for (var type : new ServerboundLoadingScreenPacketType[]{
                ServerboundLoadingScreenPacketType.START_LOADING_SCREEN,
                ServerboundLoadingScreenPacketType.END_LOADING_SCREEN}) {
            var packet = new ServerboundLoadingScreenPacket();
            packet.setType(type);
            packet.setLoadingScreenId(42);
            ByteBuf buffer = Unpooled.buffer();
            try {
                ServerboundLoadingScreenSerializer_v2193.INSTANCE.serialize(buffer, helper, packet);
                assertEquals(type == ServerboundLoadingScreenPacketType.START_LOADING_SCREEN ? 0 : 1,
                        VarInts.readInt(buffer));
                assertEquals(42, buffer.readIntLE());
                assertFalse(buffer.isReadable());
                buffer.clear();
                ServerboundLoadingScreenSerializer_v2193.INSTANCE.serialize(buffer, helper, packet);
                var decoded = new ServerboundLoadingScreenPacket();
                ServerboundLoadingScreenSerializer_v2193.INSTANCE.deserialize(buffer, helper, decoded);
                assertEquals(packet, decoded);
                assertFalse(buffer.isReadable());
            } finally {
                buffer.release();
            }
        }
    }

    @Test
    void emptyScreenIdUsesFourByteZeroSentinel() {
        var helper = Bedrock_v2193.CODEC.createHelper();
        var packet = new ServerboundLoadingScreenPacket();
        packet.setType(ServerboundLoadingScreenPacketType.START_LOADING_SCREEN);
        ByteBuf buffer = Unpooled.buffer();
        try {
            ServerboundLoadingScreenSerializer_v2193.INSTANCE.serialize(buffer, helper, packet);
            assertEquals(5, buffer.readableBytes());
            var decoded = new ServerboundLoadingScreenPacket();
            ServerboundLoadingScreenSerializer_v2193.INSTANCE.deserialize(buffer, helper, decoded);
            assertNull(decoded.getLoadingScreenId());
        } finally {
            buffer.release();
        }
    }

    @Test
    void playerActionUsesMojang2193WireIdRatherThanLegacyOrdinal() {
        var helper = Bedrock_v2193.CODEC.createHelper();
        assertEquals(14, PlayerActionType.DIMENSION_CHANGE_SUCCESS.ordinal(),
                "Do not shift shared legacy enum ordinals");
        var packet = new PlayerActionPacket();
        packet.setRuntimeEntityId(1);
        packet.setBlockPosition(Vector3i.ZERO);
        packet.setResultPosition(Vector3i.ZERO);
        packet.setAction(PlayerActionType.DIMENSION_CHANGE_SUCCESS);
        ByteBuf buffer = Unpooled.buffer();
        try {
            PlayerActionSerializer_v2193.INSTANCE.serialize(buffer, helper, packet);
            assertEquals(1L, VarInts.readUnsignedLong(buffer));
            assertEquals(13, VarInts.readInt(buffer), "2193 ChangeDimensionAck must be 13");
            buffer.clear();
            PlayerActionSerializer_v2193.INSTANCE.serialize(buffer, helper, packet);
            var decoded = new PlayerActionPacket();
            PlayerActionSerializer_v2193.INSTANCE.deserialize(buffer, helper, decoded);
            assertEquals(packet, decoded);
        } finally {
            buffer.release();
        }
    }

    @Test
    void playerActionMapsMovementAndUseItemIdsExplicitly() {
        var helper = Bedrock_v2193.CODEC.createHelper();
        var packet = new PlayerActionPacket();
        packet.setRuntimeEntityId(2);
        packet.setBlockPosition(Vector3i.ZERO);
        packet.setResultPosition(Vector3i.ZERO);
        var expected = new Object[][]{
                {PlayerActionType.START_BREAK, 1},
                {PlayerActionType.START_SWIMMING, 18},
                {PlayerActionType.START_ITEM_USE_ON, 24},
                {PlayerActionType.HANDLED_TELEPORT, 26},
                {PlayerActionType.INTERNAL_UPDATE, 33}
        };
        for (var entry : expected) {
            packet.setAction((PlayerActionType) entry[0]);
            ByteBuf buffer = Unpooled.buffer();
            try {
                PlayerActionSerializer_v2193.INSTANCE.serialize(buffer, helper, packet);
                VarInts.readUnsignedLong(buffer);
                assertEquals(entry[1], VarInts.readInt(buffer), entry[0].toString());
            } finally {
                buffer.release();
            }
        }
    }
}
