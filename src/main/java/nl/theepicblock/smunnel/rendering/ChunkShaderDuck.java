package nl.theepicblock.smunnel.rendering;

import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformFloat;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformInt;

public interface ChunkShaderDuck {
	GlUniformFloat smunnel$getStartTunnel();
	GlUniformFloat smunnel$getEndTunnel();
	GlUniformInt smunnel$getEnabled();
}
