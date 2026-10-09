package org.cloudburstmc.protocol.bedrock.codec.v2193.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.protocol.bedrock.codec.v2193.Bedrock_v2193;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.response.ItemStackResponse;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.response.ItemStackResponseStatus;
import org.cloudburstmc.protocol.bedrock.packet.ItemStackResponsePacket;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ItemStackResponseSerializer_v2193Test {

    @Test
    void emptyResponseUsesSinglePresenceByte() {
        var helper = Bedrock_v2193.CODEC.createHelper();
        var packet = new ItemStackResponsePacket();
        packet.getEntries().add(new ItemStackResponse(ItemStackResponseStatus.ERROR, -5, List.of()));
        ByteBuf buffer = Unpooled.buffer();

        try {
            ItemStackResponseSerializer_v2193.INSTANCE.serialize(buffer, helper, packet);

            assertEquals("01010900", ByteBufUtil.hexDump(buffer));

            var decoded = new ItemStackResponsePacket();
            ItemStackResponseSerializer_v2193.INSTANCE.deserialize(buffer, helper, decoded);

            assertEquals(ItemStackResponseStatus.ERROR, decoded.getEntries().getFirst().result());
            assertFalse(decoded.getEntries().getFirst().success());
            assertEquals(List.of(), decoded.getEntries().getFirst().containers());
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }
}
