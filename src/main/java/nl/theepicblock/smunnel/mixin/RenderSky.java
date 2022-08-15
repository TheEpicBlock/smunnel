package nl.theepicblock.smunnel.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferRenderer;
import com.mojang.blaze3d.vertex.Tessellator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.gl.tessellation.GlTessellation;
import me.jellysquid.mods.sodium.client.render.chunk.RegionChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.ShaderProgram;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import nl.theepicblock.smunnel.rendering.ChunkShaderDuck;
import nl.theepicblock.smunnel.rendering.MainRenderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public class RenderSky {
	@Redirect(method = "renderSky", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferRenderer;drawWithShader(Lcom/mojang/blaze3d/vertex/BufferBuilder$RenderedBuffer;)V"))
	private void redirectDrawBatch(BufferBuilder.RenderedBuffer renderedBuffer) {
		var vertexBuffer = BufferRendererAccessor.callUpload(renderedBuffer);
		if (vertexBuffer == null) return;

		vertexBuffer.setShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());

		MainRenderManager.swapToAlt();
		vertexBuffer.setShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
		MainRenderManager.swapToOriginal();
	}

	@Redirect(method = "renderSky", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;setShader(Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/ShaderProgram;)V"))
	private void redirectDrawBatch(VertexBuffer instance, Matrix4f viewMatrix, Matrix4f projectionMatrix, ShaderProgram shader) {
		instance.setShader(viewMatrix, projectionMatrix, shader);

		MainRenderManager.swapToAlt();
		instance.setShader(viewMatrix, projectionMatrix, shader);
		MainRenderManager.swapToOriginal();
	}

	@Redirect(method = "renderEndSky", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/Tessellator;draw()V"))
	private void redirectDrawBatch(Tessellator instance) {
		var vertexBuffer = BufferRendererAccessor.callUpload(instance.getBufferBuilder().end());
		if (vertexBuffer == null) return;

		vertexBuffer.setShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());

		MainRenderManager.swapToAlt();
		vertexBuffer.setShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
		MainRenderManager.swapToOriginal();
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V"))
	private void redirectClear(int i, boolean bl) {
		RenderSystem.clear(i, bl);

		MainRenderManager.swapToAlt();
		RenderSystem.clear(i, bl);
		MainRenderManager.swapToOriginal();
	}
}
