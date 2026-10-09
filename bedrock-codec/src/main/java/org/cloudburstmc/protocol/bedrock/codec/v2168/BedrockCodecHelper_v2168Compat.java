package org.cloudburstmc.protocol.bedrock.codec.v2168;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.EntityDataTypeMap;
import org.cloudburstmc.protocol.bedrock.data.Ability;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerSlotType;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.TextProcessingEventOrigin;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.action.ItemStackRequestActionType;
import org.cloudburstmc.protocol.bedrock.data.skin.AnimationData;
import org.cloudburstmc.protocol.bedrock.data.skin.PersonaPieceData;
import org.cloudburstmc.protocol.bedrock.data.skin.PersonaPieceTintData;
import org.cloudburstmc.protocol.bedrock.data.skin.PersonaPieceType;
import org.cloudburstmc.protocol.bedrock.data.skin.SerializedSkin;
import org.cloudburstmc.protocol.bedrock.util.TypeMap;
import org.cloudburstmc.protocol.bedrock.util.VarInts;

import java.awt.Color;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public class BedrockCodecHelper_v2168Compat extends BedrockCodecHelper_v2168 {
    public BedrockCodecHelper_v2168Compat(
            EntityDataTypeMap entityData,
            TypeMap<Class<?>> gameRulesTypes,
            TypeMap<ItemStackRequestActionType> stackRequestActionTypes,
            TypeMap<ContainerSlotType> containerSlotTypes,
            TypeMap<Ability> abilities,
            TypeMap<TextProcessingEventOrigin> textProcessingEventOrigins
    ) {
        super(entityData, gameRulesTypes, stackRequestActionTypes, containerSlotTypes, abilities, textProcessingEventOrigins);
    }

    @Override
    public void writeSkin(ByteBuf buffer, SerializedSkin skin) {
        requireNonNull(skin, "Skin is null");

        this.writeString(buffer, skin.getSkinId());
        this.writeString(buffer, skin.getPlayFabId());
        this.writeString(buffer, skin.getSkinResourcePatch());
        this.writeImage(buffer, skin.getSkinData());

        List<AnimationData> animations = skin.getAnimations();
        VarInts.writeUnsignedInt(buffer, animations.size());
        for (AnimationData animation : animations) {
            this.writeAnimationData(buffer, animation);
        }

        this.writeImage(buffer, skin.getCapeData());
        this.writeString(buffer, skin.getGeometryData());
        this.writeString(buffer, skin.getGeometryDataEngineVersion());
        this.writeString(buffer, skin.getAnimationData());
        this.writeString(buffer, skin.getCapeId());
        this.writeString(buffer, skin.getFullSkinId());

        buffer.writeByte("slim".equalsIgnoreCase(skin.getArmSize()) ? 0 : 1);
        Color skinColor = skin.getColor();
        buffer.writeIntLE(skinColor != null ? skinColor.getRGB() : parseLegacySkinColor(skin.getSkinColor()));

        List<PersonaPieceData> pieces = skin.getPersonaPieces();
        VarInts.writeUnsignedInt(buffer, pieces.size());
        for (PersonaPieceData piece : pieces) {
            this.writeString(buffer, piece.id());
            buffer.writeIntLE(PersonaPieceType.fromName(piece.type()).ordinal());
            this.writeUuid(buffer, UUID.fromString(piece.packId()));
            buffer.writeBoolean(piece.isDefault());
            this.writeString(buffer, piece.productId());
        }

        List<PersonaPieceTintData> tints = skin.getTintColors();
        VarInts.writeUnsignedInt(buffer, tints.size());
        for (PersonaPieceTintData tint : tints) {
            this.writeString(buffer, PersonaPieceType.fromName(tint.type()).getSerializeName());
            List<String> colors = tint.colors();
            if (colors.size() != 4) {
                throw new IllegalArgumentException("Expected 4 colors in PersonaPieceTintData");
            }
            for (String color : colors) {
                buffer.writeIntLE(parseLegacySkinColor(color));
            }
        }

        buffer.writeBoolean(skin.isPremium());
        buffer.writeBoolean(skin.isPersona());
        buffer.writeBoolean(skin.isCapeOnClassic());
        buffer.writeBoolean(skin.isPrimaryUser());
        buffer.writeBoolean(skin.isOverridingPlayerAppearance());
        this.writeString(buffer, Boolean.toString(skin.isTrusted()));
        this.writeString(buffer, skin.getProfileHash());
    }

    private static int parseLegacySkinColor(String color) {
        if (color == null || color.isBlank()) {
            return 0;
        }
        String value = color.charAt(0) == '#' ? color.substring(1) : color;
        return (int) Long.parseLong(value, 16);
    }
}
