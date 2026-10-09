package org.cloudburstmc.protocol.bedrock.codec.v2193;

import org.cloudburstmc.protocol.bedrock.data.MemoryCategoryCounter;
import org.cloudburstmc.protocol.bedrock.data.PacketRecipient;
import org.cloudburstmc.protocol.bedrock.packet.RecordStartedPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetPlayerFurnaceOptionsPacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Bedrock_v2193Test {

    @Test
    void exposesMinecraft12652ProtocolIdentity() {
        assertEquals(2193, Bedrock_v2193.CODEC.getProtocolVersion());
        assertEquals("1.26.52", Bedrock_v2193.CODEC.getMinecraftVersion());
    }

    @Test
    void registersTheNew2193PacketsAtTheirWireIds() {
        var furnace = Bedrock_v2193.CODEC.getPacketDefinition(SetPlayerFurnaceOptionsPacket.class);
        var record = Bedrock_v2193.CODEC.getPacketDefinition(RecordStartedPacket.class);

        assertEquals(351, furnace.id());
        assertEquals(PacketRecipient.BOTH, furnace.recipient());
        assertEquals(352, record.id());
        assertEquals(PacketRecipient.CLIENT, record.recipient());
    }

    @Test
    void usesThe2193MemoryCategoryIdsWithoutDependingOnEnumOrdinals() {
        assertEquals(5, Bedrock_v2193.MEMORY_CATEGORY_TYPES_2193.getId(
                MemoryCategoryCounter.Category.BLOCK_TICKING_QUEUES));
        assertEquals(43, Bedrock_v2193.MEMORY_CATEGORY_TYPES_2193.getId(
                MemoryCategoryCounter.Category.LIGHT_VOLUME_MANAGER));
        assertEquals(55, Bedrock_v2193.MEMORY_CATEGORY_TYPES_2193.getId(
                MemoryCategoryCounter.Category.ORE_UI_CLIENT));
        assertEquals(109, Bedrock_v2193.MEMORY_CATEGORY_TYPES_2193.getId(
                MemoryCategoryCounter.Category.GAMEFACE_LAYOUT));
    }
}

