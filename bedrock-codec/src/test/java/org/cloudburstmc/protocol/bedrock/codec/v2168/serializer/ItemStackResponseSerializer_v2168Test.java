package org.cloudburstmc.protocol.bedrock.codec.v2168.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.protocol.bedrock.codec.v2168.Bedrock_v2168;
import org.cloudburstmc.protocol.bedrock.codec.v407.Bedrock_v407;
import org.cloudburstmc.protocol.bedrock.codec.v407.serializer.ItemStackResponseSerializer_v407;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerSlotType;
import org.cloudburstmc.protocol.bedrock.data.inventory.FullContainerName;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.response.ItemStackResponse;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.response.ItemStackResponseContainer;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.response.ItemStackResponseSlot;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.response.ItemStackResponseStatus;
import org.cloudburstmc.protocol.bedrock.packet.ItemStackResponsePacket;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemStackResponseSerializer_v2168Test {

    @Test
    void statusBasedResponsesKeepTheLegacySuccessFlagConsistent() {
        for (var status : ItemStackResponseStatus.values()) {
            var response = new ItemStackResponse(status, -3, List.of());

            assertEquals(status == ItemStackResponseStatus.OK, response.success());
            assertEquals(status, response.result());
        }
    }

    @Test
    void statusBasedSuccessfulResponsesRemainCompatibleWithLegacySerializers() {
        var helper = Bedrock_v407.CODEC.createHelper();
        var packet = new ItemStackResponsePacket();
        packet.getEntries().add(new ItemStackResponse(ItemStackResponseStatus.OK, -3, List.of()));
        ByteBuf buffer = Unpooled.buffer();

        try {
            ItemStackResponseSerializer_v407.INSTANCE.serialize(buffer, helper, packet);

            assertEquals("01010500", ByteBufUtil.hexDump(buffer));

            var decoded = new ItemStackResponsePacket();
            ItemStackResponseSerializer_v407.INSTANCE.deserialize(buffer, helper, decoded);

            assertTrue(decoded.getEntries().getFirst().success());
            assertEquals(ItemStackResponseStatus.OK, decoded.getEntries().getFirst().result());
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test
    void rejectedResponseWithNullContainersEncodesAsEmpty() {
        var helper = Bedrock_v2168.CODEC.createHelper();
        var packet = new ItemStackResponsePacket();
        packet.getEntries().add(new ItemStackResponse(
                ItemStackResponseStatus.ERROR,
                -5,
                null
        ));
        ByteBuf buffer = Unpooled.buffer();

        ItemStackResponseSerializer_v2168.INSTANCE.serialize(buffer, helper, packet);

        // status=ERROR(1), requestId=varint(-5→9), presence=true, hasContainers=false
        assertEquals("0101090100", ByteBufUtil.hexDump(buffer));

        var decoded = new ItemStackResponsePacket();
        ItemStackResponseSerializer_v2168.INSTANCE.deserialize(buffer, helper, decoded);

        assertEquals(ItemStackResponseStatus.ERROR, decoded.getEntries().getFirst().result());
        assertFalse(decoded.getEntries().getFirst().success());
        assertEquals(-5, decoded.getEntries().getFirst().requestId());
        assertEquals(List.of(), decoded.getEntries().getFirst().containers());
        assertEquals(0, buffer.readableBytes());
    }

    @Test
    void dropResponseUsesV2168PresenceFraming() {
        var helper = Bedrock_v2168.CODEC.createHelper();
        var containerName = new FullContainerName(ContainerSlotType.INVENTORY, null);
        var slot = new ItemStackResponseSlot(25, 25, 0, 0, "", 0, "");
        var container = new ItemStackResponseContainer(
                ContainerSlotType.INVENTORY,
                List.of(slot),
                containerName
        );
        var packet = new ItemStackResponsePacket();
        packet.getEntries().add(new ItemStackResponse(
                ItemStackResponseStatus.OK,
                -3,
                List.of(container)
        ));
        ByteBuf buffer = Unpooled.buffer();

        ItemStackResponseSerializer_v2168.INSTANCE.serialize(buffer, helper, packet);

        // This is the response shape emitted for the DROP request captured in
        // server.log. Keep the fork-specific v2168 presence framing stable.
        assertEquals(
                "0100050101011d00011919000100000000",
                ByteBufUtil.hexDump(buffer)
        );

        var decoded = new ItemStackResponsePacket();
        ItemStackResponseSerializer_v2168.INSTANCE.deserialize(buffer, helper, decoded);

        assertEquals(packet.getEntries(), decoded.getEntries());
        assertTrue(decoded.getEntries().getFirst().success());
        assertEquals(0, buffer.readableBytes());
    }
}
