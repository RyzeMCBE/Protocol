package org.cloudburstmc.protocol.bedrock.codec.v2193.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.protocol.bedrock.codec.v2193.Bedrock_v2193;
import org.cloudburstmc.protocol.bedrock.data.definitions.DimensionDefinition;
import org.cloudburstmc.protocol.bedrock.util.VarInts;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DimensionDataSerializer_v2193Test {

    @Test
    void writesMinimumHeightAndHeightRange() {
        var helper = Bedrock_v2193.CODEC.createHelper();
        var definition = new DimensionDefinition(
                "minecraft:test", 319, -64, 1, 0, new UUID(1, 2), "minecraft:plains"
        );
        ByteBuf buffer = Unpooled.buffer();

        try {
            DimensionDataSerializer_v2193.INSTANCE.writeDefinition(buffer, helper, definition);

            assertEquals("minecraft:test", helper.readString(buffer));
            assertEquals(-64, VarInts.readInt(buffer));
            assertEquals(383, VarInts.readInt(buffer));
            assertEquals(1, VarInts.readInt(buffer));
            assertEquals(0, VarInts.readInt(buffer));
            assertEquals(new UUID(1, 2), helper.readUuid(buffer));
            assertEquals("minecraft:plains", helper.readString(buffer));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test
    void roundTripRestoresMaximumHeightFromRange() {
        var helper = Bedrock_v2193.CODEC.createHelper();
        var definition = new DimensionDefinition(
                "minecraft:test", 255, 0, 2, 2, new UUID(3, 4), "minecraft:the_end"
        );
        ByteBuf buffer = Unpooled.buffer();

        try {
            DimensionDataSerializer_v2193.INSTANCE.writeDefinition(buffer, helper, definition);
            assertEquals(definition, DimensionDataSerializer_v2193.INSTANCE.readDefinition(buffer, helper));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }
}
