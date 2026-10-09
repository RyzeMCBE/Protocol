package org.cloudburstmc.protocol.bedrock.codec.v2168.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.v2168.Bedrock_v2168;
import org.cloudburstmc.protocol.bedrock.codec.v2168.Bedrock_v2168_hotfix4;
import org.cloudburstmc.protocol.bedrock.codec.v2168.Bedrock_v2168_hotfix5;
import org.cloudburstmc.protocol.bedrock.data.skin.ImageData;
import org.cloudburstmc.protocol.bedrock.data.skin.PersonaPieceData;
import org.cloudburstmc.protocol.bedrock.data.skin.PersonaPieceTintData;
import org.cloudburstmc.protocol.bedrock.data.skin.SerializedSkin;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonaSkinSerializer_v2168Test {

    @Test
    void personaTintIdentifiersAreCanonicalAcrossThe2168HotfixFamily() {
        for (BedrockCodec codec : List.of(
                Bedrock_v2168.CODEC,
                Bedrock_v2168_hotfix4.CODEC,
                Bedrock_v2168_hotfix5.CODEC
        )) {
            var helper = codec.createHelper();
            ByteBuf buffer = Unpooled.buffer();

            try {
                helper.writeSkin(buffer, personaSkin());

                String payload = buffer.toString(StandardCharsets.ISO_8859_1);
                assertFalse(payload.contains("persona_mouth"), codec.getMinecraftVersion());
                assertFalse(payload.contains("persona_eyes"), codec.getMinecraftVersion());
                assertFalse(payload.contains("persona_hair"), codec.getMinecraftVersion());
                assertTrue(payload.contains("mouth"), codec.getMinecraftVersion());
                assertTrue(payload.contains("eyes"), codec.getMinecraftVersion());
                assertTrue(payload.contains("hair"), codec.getMinecraftVersion());

                SerializedSkin decoded = helper.readSkin(buffer);
                assertTrue(decoded.isPersona(), codec.getMinecraftVersion());
                assertEquals(256, decoded.getSkinData().getWidth());
                assertEquals(256, decoded.getSkinData().getHeight());
                assertEquals(List.of("mouth", "eyes", "hair"),
                        decoded.getTintColors().stream().map(PersonaPieceTintData::type).toList());
                assertEquals(0xff7e5337, decoded.getColor().getRGB());
                assertEquals(0, buffer.readableBytes());
            } finally {
                buffer.release();
            }
        }
    }

    private static SerializedSkin personaSkin() {
        return SerializedSkin.builder()
                .skinId("persona-test")
                .skinResourcePatch("{\"geometry\":{\"default\":\"geometry.persona_test\"}}")
                .skinData(ImageData.of(256, 256, new byte[256 * 256 * 4]))
                .geometryData("{}")
                .persona(true)
                .skinColor("#ff7e5337")
                .personaPieces(List.of(new PersonaPieceData(
                        "piece", "persona_skin", "2099de18-429a-465a-a49b-fc4710a17bb3", true, ""
                )))
                .tintColors(List.of(
                        new PersonaPieceTintData("persona_mouth", List.of("#0", "#0", "#ff45220e", "#0")),
                        new PersonaPieceTintData("persona_eyes", List.of("#ff381d00", "#ff1b110d", "#ffe9ecec", "#0")),
                        new PersonaPieceTintData("persona_hair", List.of("#ff1b110d", "#0", "#0", "#0"))
                ))
                .build();
    }
}
