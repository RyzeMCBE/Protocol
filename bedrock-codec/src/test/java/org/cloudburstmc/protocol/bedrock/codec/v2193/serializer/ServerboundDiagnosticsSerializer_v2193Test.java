package org.cloudburstmc.protocol.bedrock.codec.v2193.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.codec.v2193.Bedrock_v2193;
import org.cloudburstmc.protocol.bedrock.packet.ServerboundDiagnosticsPacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerboundDiagnosticsSerializer_v2193Test {

    @Test
    void entityDiagnosticsCarryPositionAndDimensionWithoutDuplicatingArrays() {
        var helper = Bedrock_v2193.CODEC.createHelper();
        var packet = new ServerboundDiagnosticsPacket();
        var entity = new ServerboundDiagnosticsPacket.EntityDiagnostics();
        entity.setDisplayName("Zombie");
        entity.setEntity("minecraft:zombie");
        entity.setTimeInNs(1234L);
        entity.setPercentOfTotal(17);
        entity.setPosition(Vector3f.from(1.5f, 64f, -2.25f));
        entity.setDimension("minecraft:overworld");
        packet.getEntityDiagnostics().add(entity);

        ByteBuf buffer = Unpooled.buffer();
        try {
            var serializer = (ServerboundDiagnosticsSerializer_v2193)
                    Bedrock_v2193.CODEC.getPacketDefinition(ServerboundDiagnosticsPacket.class).serializer();
            serializer.serialize(buffer, helper, packet);

            var decoded = new ServerboundDiagnosticsPacket();
            serializer.deserialize(buffer, helper, decoded);

            assertEquals(1, decoded.getEntityDiagnostics().size());
            assertEquals(entity, decoded.getEntityDiagnostics().getFirst());
            assertEquals(0, decoded.getSystemDiagnostics().size());
            assertEquals(0, decoded.getSystemCategories().size());
            assertEquals(0, decoded.getWhiskerScopes().size());
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }
}
