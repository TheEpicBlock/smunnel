package nl.theepicblock.smunnel.mixin;

import com.mojang.blaze3d.vertex.*;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.tessellation.GlTessellation;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.*;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import nl.theepicblock.smunnel.Smunnel;
import nl.theepicblock.smunnel.SmunnelClient;
import nl.theepicblock.smunnel.rendering.ChunkShaderDuck;
import nl.theepicblock.smunnel.rendering.MainRenderManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
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

		if (MainRenderManager.shouldRenderAlt()) {
			ChunkShaderInterface shader = this.activeProgram.getInterface();
			var duck = (ChunkShaderDuck)shader;

			duck.smunnel$getEnabled().set(1);
			MainRenderManager.swapToAlt();
			executeDrawBatches(batch, i);
			duck.smunnel$getEnabled().set(0);
			MainRenderManager.swapToOriginal();
		}
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void endBlockRendering(ChunkRenderMatrices matrices, CommandList commandList, ChunkRenderList list, BlockRenderPass pass, ChunkCameraContext camera, CallbackInfo ci) {
//		SmunnelClient.PORTAL_SHADER.get().bind();
//
//		var tex = MainRenderManager.altBuffer.getColorAttachment();
//
//		GL20C.glActiveTexture(GL20C.GL_TEXTURE0);
//		GL20C.glBindTexture(GL20C.GL_TEXTURE_2D, tex);
//
//		BufferBuilder bufferBuilder = Tessellator.getInstance().getBufferBuilder();
//		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
//		bufferBuilder.vertex(-1.0, 0.0, 0.0).next();
//		bufferBuilder.vertex(2.0, 0.0, 0).next();
//		bufferBuilder.vertex(2.0, 3, 0).next();
//		bufferBuilder.vertex(0.0, 3, 0).next();
//		BufferRenderer.draw(bufferBuilder.end());
	}

	@Inject(method = "setModelMatrixUniforms", at = @At("HEAD"))
	private void onSetUniforms(ChunkShaderInterface shader, RenderRegion region, ChunkCameraContext camera, CallbackInfo ci) {
		var duck = (ChunkShaderDuck)shader;
		duck.smunnel$getStartTunnel().setFloat(0-camera.posZ);
		duck.smunnel$getEndTunnel().setFloat(-8-camera.posZ);
	}
}
