package org.cloudburstmc.protocol.bedrock.codec.v2169;

import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.v2168.Bedrock_v2168_hotfix4;

/**
 * Minecraft Bedrock 1.26.45 codec (protocol 2169).
 *
 * <p>1.26.45 currently reuses the packet and serializer layout from the
 * 1.26.44 compatibility codec while advertising its own wire protocol number.
 * Keeping this codec in a dedicated package prevents protocol 2168 from being
 * accidentally reported as protocol 2169.</p>
 */
public final class Bedrock_v2169 {

    public static final BedrockCodec CODEC = Bedrock_v2168_hotfix4.CODEC.toBuilder()
            .protocolVersion(2169)
            .minecraftVersion("1.26.45")
            .build();

    private Bedrock_v2169() {
    }
}
