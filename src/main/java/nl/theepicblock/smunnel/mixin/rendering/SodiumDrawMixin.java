package nl.theepicblock.smunnel.mixin.rendering;

import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.tessellation.GlTessellation;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.*;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.ShaderChunkRendererExt;
import net.coderbot.iris.shadows.ShadowRenderingState;
import nl.theepicblock.smunnel.Smunnel;
import nl.theepicblock.smunnel.rendering.ChunkShaderDuck;
import nl.theepicblock.smunnel.rendering.MainRenderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nl.theepicblock.smunnel.Smunnel.IRIS;

@Mixin(value = RegionChunkRenderer.class, remap = false)
public abstract class SodiumDrawMixin extends ShaderChunkRenderer {
	public SodiumDrawMixin(RenderDevice device, ChunkVertexType vertexType) {
		super(device, vertexType);
	}

	@Shadow
	protected abstract void executeDrawBatches(CommandList commandList, GlTessellation tessellation);

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/RegionChunkRenderer;executeDrawBatches(Lme/jellysquid/mods/sodium/client/gl/device/CommandList;Lme/jellysquid/mods/sodium/client/gl/tessellation/GlTessellation;)V"))
	private void redirectDrawBatch(RegionChunkRenderer instance, CommandList batch, GlTessellation tessellation) {
		var program = (this.activeProgram == null && this instanceof ShaderChunkRendererExt e) ? e.iris$getOverride() : this.activeProgram;
		var shader = ((ChunkShaderDuck)program.getInterface()).smunnel$getExtension();

		MainRenderManager.executeMainWithShader(shader, () -> {
			executeDrawBatches(batch, tessellation);
		});

		// We shouldn't be double-rendering shadows
		if (!(IRIS && ShadowRenderingState.areShadowsCurrentlyBeingRendered())) {
			MainRenderManager.executeAltsWithShader(shader, () -> {
				executeDrawBatches(batch, tessellation);
			});
		}
	}
}
