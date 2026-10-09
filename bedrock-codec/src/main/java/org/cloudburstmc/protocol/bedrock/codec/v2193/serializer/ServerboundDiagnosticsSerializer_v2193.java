package org.cloudburstmc.protocol.bedrock.codec.v2193.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v2168.serializer.ServerboundDiagnosticsSerializer_v2168;
import org.cloudburstmc.protocol.bedrock.data.MemoryCategoryCounter;
import org.cloudburstmc.protocol.bedrock.packet.ServerboundDiagnosticsPacket;
import org.cloudburstmc.protocol.bedrock.util.TypeMap;

public class ServerboundDiagnosticsSerializer_v2193 extends ServerboundDiagnosticsSerializer_v2168 {

    public ServerboundDiagnosticsSerializer_v2193(TypeMap<MemoryCategoryCounter.Category> memoryCategoryTypes) {
        super(memoryCategoryTypes);
    }

    @Override
    protected void writeEntityDiagnostics(ByteBuf buffer, BedrockCodecHelper helper,
                                          ServerboundDiagnosticsPacket.EntityDiagnostics diagnostics) {
        helper.writeString(buffer, diagnostics.getDisplayName());
        helper.writeString(buffer, diagnostics.getEntity());
        buffer.writeLongLE(diagnostics.getTimeInNs());
        buffer.writeByte(diagnostics.getPercentOfTotal());
        helper.writeVector3f(buffer, diagnostics.getPosition());
        helper.writeString(buffer, diagnostics.getDimension());
    }

    @Override
    protected ServerboundDiagnosticsPacket.EntityDiagnostics readEntityDiagnostics(
            ByteBuf buffer, BedrockCodecHelper helper) {
        ServerboundDiagnosticsPacket.EntityDiagnostics diagnostics =
                new ServerboundDiagnosticsPacket.EntityDiagnostics();
        diagnostics.setDisplayName(helper.readString(buffer));
        diagnostics.setEntity(helper.readString(buffer));
        diagnostics.setTimeInNs(buffer.readLongLE());
        diagnostics.setPercentOfTotal(buffer.readUnsignedByte());
        diagnostics.setPosition(helper.readVector3f(buffer));
        diagnostics.setDimension(helper.readString(buffer));
        return diagnostics;
    }
}
