package org.cloudburstmc.protocol.bedrock.codec.v2168.serializer;

import io.netty.buffer.Unpooled;
import org.cloudburstmc.protocol.bedrock.codec.v827.Bedrock_v827;
import org.cloudburstmc.protocol.bedrock.codec.v844.Bedrock_v844;
import org.cloudburstmc.protocol.bedrock.codec.v2168.Bedrock_v2168;
import org.cloudburstmc.protocol.bedrock.codec.v2168.Bedrock_v2168_hotfix4;
import org.cloudburstmc.protocol.bedrock.codec.v2168.Bedrock_v2168_hotfix5;
import org.cloudburstmc.protocol.bedrock.codec.v2193.Bedrock_v2193;
import org.cloudburstmc.protocol.bedrock.data.skin.ImageData;
import org.cloudburstmc.protocol.bedrock.data.skin.SerializedSkin;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkinColorTranslationTest {

    @Test
    void rgbAndExplicitArgbHaveDifferentAlphaDefaults() {
        assertEquals(0xff7e5337, skin("#7e5337").getColor().getRGB());
        assertEquals(0xff7e5337, skin("7e5337").getColor().getRGB());
        assertEquals(0xff000000, skin("#000000").getColor().getRGB());
        assertEquals(0x407e5337, skin("#407e5337").getColor().getRGB());
        assertEquals(0, skin("#0").getColor().getRGB());
        assertEquals(0, skin("#00000000").getColor().getRGB());
    }

    @Test
    void opaqueColorSurvivesLegacyAndModernSkinLayouts() {
        for (var codec : List.of(Bedrock_v827.CODEC, Bedrock_v844.CODEC, Bedrock_v2168.CODEC,
                Bedrock_v2168_hotfix4.CODEC, Bedrock_v2168_hotfix5.CODEC, Bedrock_v2193.CODEC)) {
            for (String color : List.of("#7e5337", "#ff7e5337")) {
                var buffer = Unpooled.buffer();
                try {
                    codec.createHelper().writeSkin(buffer, skin(color));
                    var decoded = codec.createHelper().readSkin(buffer);
                    assertEquals(0xff7e5337, decoded.getColor().getRGB(), codec.getMinecraftVersion());
                    assertEquals(0, buffer.readableBytes(), codec.getMinecraftVersion());
                    assertEquals(0xff7e5337, decoded.toBuilder().build().getColor().getRGB());
                } finally {
                    buffer.release();
                }
            }
        }
    }

    private static SerializedSkin skin(String color) {
        return SerializedSkin.builder()
                .skinId("color-fixture")
                .skinResourcePatch("{\"geometry\":{\"default\":\"geometry.humanoid.custom\"}}")
                .skinData(ImageData.of(64, 64, new byte[64 * 64 * 4]))
                .geometryData("{}")
                .skinColor(color)
                .build();
    }
}
