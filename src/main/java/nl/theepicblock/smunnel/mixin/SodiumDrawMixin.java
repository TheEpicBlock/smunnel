package nl.theepicblock.smunnel.mixin;

import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.tessellation.GlTessellation;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkCameraContext;
import me.jellysquid.mods.sodium.client.render.chunk.RegionChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import nl.theepicblock.smunnel.Smunnel;
import nl.theepicblock.smunnel.rendering.ChunkShaderDuck;
import nl.theepicblock.smunnel.rendering.MainRenderManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL32;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RegionChunkRenderer.class, remap = false)
public abstract class SodiumDrawMixin extends ShaderChunkRenderer {
	public SodiumDrawMixin(RenderDevice device, ChunkVertexType vertexType) {
		super(device, vertexType);
	}

	@Shadow
	protected abstract void executeDrawBatches(CommandList commandList, GlTessellation tessellation);

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/RegionChunkRenderer;executeDrawBatches(Lme/jellysquid/mods/sodium/client/gl/device/CommandList;Lme/jellysquid/mods/sodium/client/gl/tessellation/GlTessellation;)V"))
	private void redirectDrawBatch(RegionChunkRenderer instance, CommandList batch, GlTessellation i) {
		executeDrawBatches(batch, i);

		ChunkShaderInterface shader = this.activeProgram.getInterface();
		var duck = (ChunkShaderDuck)shader;

		duck.smunnel$getEnabled().set(1);
		MainRenderManager.swapToAlt();
		executeDrawBatches(batch, i);
		duck.smunnel$getEnabled().set(0);
		MainRenderManager.swapToOriginal();

//		var originalShaderProgram = new int[1];
//		GL20C.glGetIntegerv(GL20C.GL_CURRENT_PROGRAM, originalShaderProgram); // Store the currently used program
//		Smunnel.LOGGER.info("Currently rendering "+originalShaderProgram[0]);
	}

	@Inject(method = "setModelMatrixUniforms", at = @At("HEAD"))
	private void onSetUniforms(ChunkShaderInterface shader, RenderRegion region, ChunkCameraContext camera, CallbackInfo ci) {
		var duck = (ChunkShaderDuck)shader;
		duck.smunnel$getStartTunnel().setFloat(0-camera.posZ);
		duck.smunnel$getEndTunnel().setFloat(-8-camera.posZ);
	}
}
