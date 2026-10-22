package org.cloudburstmc.protocol.bedrock.codec.v2193;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventorySource;
import org.cloudburstmc.protocol.bedrock.util.VarInts;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BedrockCodecHelper_v2193Test {

    @Test
    void untrackedInteractionUiSourcePreservesContainerIdAndPresenceFlags() {
        var helper = Bedrock_v2193.CODEC.createHelper();
        var source = InventorySource.fromUntrackedInteractionUI(42);
        ByteBuf buffer = Unpooled.buffer();

        try {
            helper.writeSource(buffer, source);

            assertEquals(InventorySource.Type.UNTRACKED_INTERACTION_UI.id(),
                    VarInts.readUnsignedInt(buffer));
            assertTrue(buffer.readBoolean(), "Container ID must be present");
            assertEquals(42, buffer.readUnsignedByte());
            assertFalse(buffer.readBoolean(), "No world interaction flag is present");
            assertFalse(buffer.isReadable());

            buffer.readerIndex(0);
            assertEquals(source, helper.readSource(buffer));
            assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }

    @Test
    void otherInventorySourceKindsRetainTheir2193RoundTrip() {
        var helper = Bedrock_v2193.CODEC.createHelper();

        for (var source : List.of(
                InventorySource.fromContainerWindowId(3),
                InventorySource.fromNonImplementedTodo(17),
                InventorySource.fromWorldInteraction(InventorySource.Flag.DROP_ITEM),
                InventorySource.fromGlobalInventory(),
                InventorySource.fromCreativeInventory())) {
            ByteBuf buffer = Unpooled.buffer();
            try {
                helper.writeSource(buffer, source);
                assertEquals(source, helper.readSource(buffer), source.toString());
                assertFalse(buffer.isReadable());
            } finally {
                buffer.release();
            }
        }
    }
}
