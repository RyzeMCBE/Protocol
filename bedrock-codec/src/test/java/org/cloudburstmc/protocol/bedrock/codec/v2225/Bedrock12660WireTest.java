package org.cloudburstmc.protocol.bedrock.codec.v2225;

import io.netty.buffer.Unpooled;
import org.cloudburstmc.protocol.bedrock.codec.v2193.Bedrock_v2193;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.ItemStackRequest;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.action.*;
import org.cloudburstmc.protocol.bedrock.data.skin.ImageData;
import org.cloudburstmc.protocol.bedrock.data.skin.SerializedSkin;
import org.cloudburstmc.protocol.bedrock.data.skin.PersonaPieceData;
import org.cloudburstmc.protocol.bedrock.data.BuildPlatform;
import org.cloudburstmc.protocol.bedrock.data.definitions.DimensionDefinition;
import org.cloudburstmc.protocol.bedrock.packet.*;
import org.cloudburstmc.protocol.bedrock.util.VarInts;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class Bedrock12660WireTest {
    @Test
    void registersAllEightNewPacketsWithoutReplacingExistingIds() {
        Class<?>[] types = {ClientboundMatchmakingStatePacket.class, ServerboundStonecutterSetRecipePacket.class,
                ClientboundStonecutterSetRecipePacket.class, ServerboundMatchmakingCancelPacket.class,
                SetPassengerOfBlockPacket.class, ServerboundCursorItemDragPacket.class,
                ClientboundPlayAudioContentPacket.class, ServerboundRegisterAudioContentPacket.class};
        for (int i = 0; i < types.length; i++) {
            assertEquals(types[i], Bedrock_v2225.CODEC.getPacketDefinition(353 + i).factory().get().getClass());
            assertNull(Bedrock_v2193.CODEC.getPacketDefinition(353 + i));
        }
    }

    @Test
    void craftingUsesAVariantIndexSeparateFromItsWireEnum() {
        var buffer = Unpooled.buffer();
        try {
            var helper = Bedrock_v2225.CODEC.createHelper();
            var request = new ItemStackRequest(3, new ItemStackRequestAction[]{new CraftRecipeAction(42, 1)}, new String[0]);
            helper.writeItemStackRequest(buffer, request);
            var inspection = buffer.duplicate();
            assertEquals(3, VarInts.readInt(inspection));
            assertEquals(1, VarInts.readUnsignedInt(inspection));
            assertEquals(10, VarInts.readUnsignedInt(inspection));
            assertEquals(12, inspection.readUnsignedByte());
            assertEquals(42, VarInts.readUnsignedInt(inspection));
            assertEquals(1, inspection.readUnsignedByte());
            assertArrayEquals(request.actions(), helper.readItemStackRequest(buffer).actions());
            assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }

    @Test
    void reservedActionRoundTripsWithoutShiftingTheFollowingAction() {
        var buffer = Unpooled.buffer();
        try {
            var helper = Bedrock_v2225.CODEC.createHelper();
            var request = new ItemStackRequest(7, new ItemStackRequestAction[]{
                    new CraftReservedAction("fixture", 2), new CraftRecipeAction(55, 3)}, new String[0]);
            helper.writeItemStackRequest(buffer, request);
            assertArrayEquals(request.actions(), helper.readItemStackRequest(buffer).actions());
            assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }

    @Test
    void skinOmitsPlayFabFromItsBodyAndPreservesLargeTexturesAndOpaqueColors() {
        var skin = SerializedSkin.builder().skinId("skin-fixture").playFabId("playfab-fixture")
                .skinResourcePatch("{\"geometry\":{\"default\":\"geometry.humanoid.custom\"}}")
                .skinData(ImageData.of(256, 256, new byte[256 * 256 * 4]))
                .geometryData("{}").skinColor("#7e5337").profileHash("profile-fixture").build();
        var buffer = Unpooled.buffer();
        try {
            var helper = Bedrock_v2225.CODEC.createHelper();
            helper.writeSkin(buffer, skin);
            var inspection = buffer.duplicate();
            assertEquals(skin.getSkinId(), helper.readString(inspection));
            assertEquals(skin.getSkinResourcePatch(), helper.readString(inspection));
            var decoded = helper.readSkin(buffer);
            assertEquals(256, decoded.getSkinData().getWidth());
            assertEquals(0xff7e5337, decoded.getColor().getRGB());
            assertEquals("profile-fixture", decoded.getProfileHash());
            assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }

    @Test
    void newPersonaPieceUsesItsNewIdAndFallsBackForOlderClients() {
        var skin = SerializedSkin.builder().skinId("persona-fixture")
                .skinResourcePatch("{\"geometry\":{\"default\":\"geometry.humanoid.custom\"}}")
                .geometryData("{}").profileHash("fixture")
                .skinData(ImageData.of(64, 64, new byte[64 * 64 * 4]))
                .skinColor("#7e5337").personaPieces(List.of(new PersonaPieceData(
                        "piece", "persona_coco", new UUID(0, 0).toString(), false, ""))).build();
        for (var codec : List.of(Bedrock_v2193.CODEC, Bedrock_v2225.CODEC)) {
            var buffer = Unpooled.buffer();
            try {
                var helper = codec.createHelper();
                helper.writeSkin(buffer, skin);
                var decoded = helper.readSkin(buffer);
                assertEquals(codec.getProtocolVersion() == 2225 ? "coco" : "unsupported",
                        decoded.getPersonaPieces().getFirst().type());
                assertFalse(buffer.isReadable());
            } finally {
                buffer.release();
            }
        }
    }

    @Test
    void playerListMovesPlayFabBeforePlatformChatAndRetainsSkinTrust() {
        var skin = SerializedSkin.builder().skinId("fixture").playFabId("playfab-fixture")
                .skinResourcePatch("{\"geometry\":{\"default\":\"geometry.humanoid.custom\"}}")
                .geometryData("{}").profileHash("fixture").skinColor("#ff7e5337")
                .skinData(ImageData.of(64, 64, new byte[64 * 64 * 4])).trusted(true).build();
        var entry = new PlayerListPacket.Entry(new UUID(1, 2));
        entry.setAction(PlayerListPacket.Action.ADD);
        entry.setEntityId(44);
        entry.setName("fixture-player");
        entry.setXuid("123");
        entry.setPlatformChatId("platform-fixture");
        entry.setBuildPlatform(BuildPlatform.GOOGLE);
        entry.setSkin(skin);
        entry.setColor(java.awt.Color.WHITE);
        var packet = new PlayerListPacket();
        packet.getEntries().add(entry);
        var buffer = Unpooled.buffer();
        try {
            var helper = Bedrock_v2225.CODEC.createHelper();
            Bedrock_v2225.CODEC.tryEncode(helper, buffer, packet);
            var inspection = buffer.duplicate();
            assertEquals(1, VarInts.readUnsignedInt(inspection));
            assertEquals(1, VarInts.readUnsignedInt(inspection));
            assertEquals(0, inspection.readUnsignedByte());
            assertEquals(entry.getUuid(), helper.readUuid(inspection));
            assertEquals(44, VarInts.readLong(inspection));
            assertEquals(entry.getName(), helper.readString(inspection));
            assertEquals(entry.getXuid(), helper.readString(inspection));
            assertEquals("playfab-fixture", helper.readString(inspection));
            assertEquals("platform-fixture", helper.readString(inspection));
            var decoded = (PlayerListPacket) Bedrock_v2225.CODEC.tryDecode(helper, buffer, 63);
            assertEquals("playfab-fixture", decoded.getEntries().getFirst().getSkin().getPlayFabId());
            assertTrue(decoded.getEntries().getFirst().getSkin().isTrusted());
            assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }

    @Test
    void dimensionCloudSettingsAndChunkBiomeUpdateRoundTripWithoutTrailingBytes() {
        var dimensions = new DimensionDataPacket();
        dimensions.getDefinitions().add(new DimensionDefinition("minecraft:overworld", 512, -64,
                1, 0, new UUID(0, 0), "minecraft:plains", 128, true));
        var chunk = new LevelChunkPacket();
        chunk.setChunkX(-2);
        chunk.setChunkZ(3);
        chunk.setClientBiomeUpdate(true);
        chunk.setData(Unpooled.wrappedBuffer(new byte[]{1, 2, 3}));
        try {
            for (var packet : List.of(dimensions, chunk)) {
                var buffer = Unpooled.buffer();
                try {
                    var codec = Bedrock_v2225.CODEC;
                    codec.tryEncode(codec.createHelper(), buffer, packet);
                    var decoded = codec.tryDecode(codec.createHelper(), buffer,
                            codec.getPacketDefinition(packet.getClass()).id());
                    assertEquals(packet, decoded);
                    assertFalse(buffer.isReadable());
                    if (decoded instanceof LevelChunkPacket levelChunk) levelChunk.release();
                } finally {
                    buffer.release();
                }
            }
        } finally {
            chunk.release();
        }
    }
}
