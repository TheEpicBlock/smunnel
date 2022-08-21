package nl.theepicblock.smunnel.mixin.rendering.iris;

import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkShaderInterface;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.ShaderBindingContextExt;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import nl.theepicblock.smunnel.rendering.ChunkShaderDuck;
import nl.theepicblock.smunnel.rendering.MainRenderManager;
import nl.theepicblock.smunnel.rendering.SpaceCompressionShaderInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = IrisChunkShaderInterface.class, remap = false)
public abstract class ChunkShaderExtension implements ChunkShaderDuck {
	@Unique
	private SpaceCompressionShaderInterface extension;


	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(int par1, ShaderBindingContextExt par2, SodiumTerrainPipeline par3, boolean par4, BlendModeOverride par5, float par6, CallbackInfo ci) {
		extension = new SpaceCompressionShaderInterface(((GlProgram<?>)par2).handle());
	}

	@Override
	public SpaceCompressionShaderInterface smunnel$getExtension() {
		return extension;
	}
}
