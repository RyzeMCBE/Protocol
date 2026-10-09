package org.cloudburstmc.protocol.bedrock.codec.v2193.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.codec.v2193.Bedrock_v2193;
import org.cloudburstmc.protocol.bedrock.data.primitiveshape.PrimitiveText;
import org.cloudburstmc.protocol.bedrock.packet.PrimitiveShapesPacket;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class PrimitiveShapesSerializer_v2193Test {

    @Test
    void textRoundTripPreservesLineGapAndRenderFlags() {
        var helper = Bedrock_v2193.CODEC.createHelper();
        var packet = new PrimitiveShapesPacket();
        packet.getShapes().add(new PrimitiveText(
                42L, 0, Vector3f.from(1, 2, 3), 1.0f, Vector3f.ZERO, 20.0f, Color.WHITE,
                "hello", true, Color.BLACK, true, false, true, 2.5f, 64.0f, null
        ));

        ByteBuf buffer = Unpooled.buffer();
        try {
            PrimitiveShapesSerializer_v2193.INSTANCE.serialize(buffer, helper, packet);

            var decoded = new PrimitiveShapesPacket();
            PrimitiveShapesSerializer_v2193.INSTANCE.deserialize(buffer, helper, decoded);

            var text = assertInstanceOf(PrimitiveText.class, decoded.getShapes().getFirst());
            assertEquals("hello", text.getText());
            assertEquals(2.5f, text.getLineGapHeight());
            assertEquals(true, text.isDepthTest());
            assertEquals(false, text.isShowBackface());
            assertEquals(true, text.isShowTextBackface());
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }
}
