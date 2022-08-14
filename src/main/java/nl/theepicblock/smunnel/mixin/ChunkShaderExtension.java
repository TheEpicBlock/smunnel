package nl.theepicblock.smunnel.mixin;

import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformFloat;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformInt;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import nl.theepicblock.smunnel.rendering.ChunkShaderDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkShaderInterface.class)
public class ChunkShaderExtension implements ChunkShaderDuck {
	@Unique
	private GlUniformFloat startTunnel;
	@Unique
	private GlUniformFloat endTunnel;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(ShaderBindingContext context, ChunkShaderOptions options, CallbackInfo ci) {
		startTunnel = context.bindUniform("tunnelStart", GlUniformFloat::new);
		endTunnel = context.bindUniform("tunnelEnd", GlUniformFloat::new);
	}

	@Override
	public GlUniformFloat smunnel$getStartTunnel() {
		return startTunnel;
	}

	@Override
	public GlUniformFloat smunnel$getEndTunnel() {
		return endTunnel;
	}
}
