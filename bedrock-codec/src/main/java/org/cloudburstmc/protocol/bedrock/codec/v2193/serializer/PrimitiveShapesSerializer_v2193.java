package org.cloudburstmc.protocol.bedrock.codec.v2193.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v1001.serializer.PrimitiveShapesSerializer_v1001;
import org.cloudburstmc.protocol.bedrock.data.primitiveshape.*;
import org.cloudburstmc.protocol.bedrock.util.VarInts;

import java.awt.*;

public class PrimitiveShapesSerializer_v2193 extends PrimitiveShapesSerializer_v1001 {

    public static final PrimitiveShapesSerializer_v2193 INSTANCE = new PrimitiveShapesSerializer_v2193();

    @Override
    protected void writeShape(ByteBuf buffer, BedrockCodecHelper helper, PrimitiveShape shape) {
        if (shape.getType() != PrimitiveShape.Type.TEXT) {
            super.writeShape(buffer, helper, shape);
            return;
        }

        writeCommonShapeData(buffer, helper, shape);
        VarInts.writeUnsignedInt(buffer, toPayloadType(PrimitiveShape.Type.TEXT));

        PrimitiveText text = (PrimitiveText) shape;
        helper.writeString(buffer, text.getText());
        buffer.writeBoolean(text.isUseRotation());
        helper.writeOptionalNull(buffer, text.getBackgroundColor(), (buf, color) -> buf.writeIntLE(color.getRGB()));
        buffer.writeFloatLE(text.getLineGapHeight());
        buffer.writeBoolean(text.isDepthTest());
        buffer.writeBoolean(text.isShowBackface());
        buffer.writeBoolean(text.isShowTextBackface());
    }

    @Override
    protected PrimitiveShape readShape(ByteBuf buffer, BedrockCodecHelper helper) {
        long id = VarInts.readUnsignedLong(buffer);

        PrimitiveShape.Type type = helper.readOptional(buffer, null, (buf, aHelper) -> SHAPE_TYPES[buf.readUnsignedByte()]);
        Vector3f position = helper.readOptional(buffer, null, READ_VECTOR3F);
        Float scale = helper.readOptional(buffer, null, ByteBuf::readFloatLE);
        Vector3f rotation = helper.readOptional(buffer, null, READ_VECTOR3F);
        Float totalTimeLeft = helper.readOptional(buffer, null, ByteBuf::readFloatLE);
        Float maximumRenderDistance = helper.readOptional(buffer, null, ByteBuf::readFloatLE);
        Color color = helper.readOptional(buffer, null, value -> new Color(value.readIntLE(), true));
        Integer dimension = helper.readOptional(buffer, -1, VarInts::readInt);
        Long attachedToEntityId = helper.readOptional(buffer, null, VarInts::readLong);
        VarInts.readUnsignedInt(buffer);

        if (type == null) {
            return new PrimitiveShape(id, dimension, position, scale, rotation, totalTimeLeft, color, maximumRenderDistance, attachedToEntityId);
        }

        return switch (type) {
            case ARROW -> new PrimitiveArrow(id, dimension, position, scale, rotation, totalTimeLeft, color,
                    maximumRenderDistance, helper.readOptional(buffer, null, READ_VECTOR3F),
                    helper.readOptional(buffer, null, ByteBuf::readFloatLE),
                    helper.readOptional(buffer, null, ByteBuf::readFloatLE),
                    helper.readOptional(buffer, null, buf -> (int) buf.readUnsignedByte()),
                    attachedToEntityId);
            case BOX -> new PrimitiveBox(id, dimension, position, scale, rotation, totalTimeLeft, color,
                    maximumRenderDistance, helper.readVector3f(buffer), attachedToEntityId);
            case CIRCLE -> new PrimitiveCircle(id, dimension, position, scale, rotation, totalTimeLeft, color,
                    maximumRenderDistance, (int) buffer.readUnsignedByte(), attachedToEntityId);
            case LINE -> new PrimitiveLine(id, dimension, position, scale, rotation, totalTimeLeft, color,
                    maximumRenderDistance, helper.readVector3f(buffer), attachedToEntityId);
            case SPHERE -> new PrimitiveSphere(id, dimension, position, scale, rotation, totalTimeLeft, color,
                    maximumRenderDistance, (int) buffer.readUnsignedByte(), attachedToEntityId);
            case TEXT -> {
                String text = helper.readString(buffer);
                boolean useRotation = buffer.readBoolean();
                Color background = helper.readOptional(buffer, null, value -> new Color(value.readIntLE(), true));
                float lineGapHeight = buffer.readFloatLE();
                boolean depthTest = buffer.readBoolean();
                boolean showBackface = buffer.readBoolean();
                boolean showTextBackface = buffer.readBoolean();
                yield new PrimitiveText(id, dimension, position, scale, rotation, totalTimeLeft, color,
                        text, useRotation, background, depthTest, showBackface, showTextBackface,
                        lineGapHeight, maximumRenderDistance, attachedToEntityId);
            }
            case CYLINDER -> {
                var radiusX = helper.readVector2f(buffer);
                var radiusZ = helper.readVector2f(buffer);
                yield new PrimitiveCylinder(id, dimension, position, scale, rotation, totalTimeLeft, color,
                        maximumRenderDistance, buffer.readFloatLE(), buffer.readUnsignedByte(),
                        radiusX, radiusZ, attachedToEntityId);
            }
            case PYRAMID -> {
                float width = buffer.readFloatLE();
                Float depth = helper.readOptional(buffer, null, ByteBuf::readFloatLE);
                yield new PrimitivePyramid(id, dimension, position, scale, rotation, totalTimeLeft, color,
                        maximumRenderDistance, buffer.readFloatLE(), width, depth, attachedToEntityId);
            }
            case ELLIPSOID -> {
                var radii = helper.readVector3f(buffer);
                yield new PrimitiveEllipsoid(id, dimension, position, scale, rotation, totalTimeLeft, color,
                        maximumRenderDistance, buffer.readUnsignedByte(), radii, attachedToEntityId);
            }
            case CONE -> {
                var radii = helper.readVector2f(buffer);
                yield new PrimitiveCone(id, dimension, position, scale, rotation, totalTimeLeft, color,
                        maximumRenderDistance, buffer.readFloatLE(), buffer.readUnsignedByte(),
                        radii, attachedToEntityId);
            }
        };
    }
}
