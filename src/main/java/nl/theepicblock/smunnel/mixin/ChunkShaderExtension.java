package nl.theepicblock.smunnel.mixin;

import me.jellysquid.mods.sodium.client.gl.GlObject;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformFloat;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformInt;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import nl.theepicblock.smunnel.rendering.ChunkShaderDuck;
import nl.theepicblock.smunnel.rendering.SpaceCompressionShaderInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkShaderInterface.class)
public abstract class ChunkShaderExtension implements ChunkShaderDuck {
	@Unique
	private SpaceCompressionShaderInterface extension;


	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(ShaderBindingContext context, ChunkShaderOptions options, CallbackInfo ci) {
		extension = new SpaceCompressionShaderInterface(((GlProgram<?>)context).handle());
	}

	@Override
	public SpaceCompressionShaderInterface smunnel$getExtension() {
		return extension;
	}
}
