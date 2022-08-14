package nl.theepicblock.smunnel.rendering;

import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformFloat;

public interface ChunkShaderDuck {
	GlUniformFloat smunnel$getStartTunnel();
	GlUniformFloat smunnel$getEndTunnel();
}
