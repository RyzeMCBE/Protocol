package org.cloudburstmc.protocol.bedrock.codec.v2168;

import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;

/**
 * Compatibility alias for the final Minecraft Bedrock 1.26.44 hotfix codec.
 *
 * <p>Protocol 2169 is represented exclusively by
 * {@code org.cloudburstmc.protocol.bedrock.codec.v2169.Bedrock_v2169}.
 * Keeping this class on 2168 prevents callers that still use the historical
 * v2168 hotfix5 name from advertising protocol 2169.</p>
 */
public class Bedrock_v2168_hotfix5 extends Bedrock_v2168_hotfix4 {

    public static final BedrockCodec CODEC = Bedrock_v2168_hotfix4.CODEC.toBuilder()
            .protocolVersion(2168)
            .minecraftVersion("1.26.44")
            .build();
}
