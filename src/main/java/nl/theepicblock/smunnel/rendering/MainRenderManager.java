package nl.theepicblock.smunnel.rendering;

import com.mojang.blaze3d.framebuffer.Framebuffer;
import com.mojang.blaze3d.framebuffer.SimpleFramebuffer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import nl.theepicblock.smunnel.SmunnelClient;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30;

import java.io.File;
import java.io.IOException;

public class MainRenderManager {
	public static Framebuffer altBuffer;
	private static int currentBuffer; // Stores the currently bound framebuffer
	private static int originalBuffer; // Temporarily stores a framebuffer whilst it's swapped to altBuffer
	private static int i = 0;

	public static void startRender() {
		swapToAlt();
		altBuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
		swapToOriginal();
	}

	static {
		var w = MinecraftClient.getInstance().getWindow();
		altBuffer = new SimpleFramebuffer(w.getFramebufferWidth(), w.getFramebufferHeight(), true, MinecraftClient.IS_SYSTEM_MAC);
		altBuffer.setClearColor(0,0,0,0);
	}

	public static void endRender(WorldRenderContext ctx) {
//		i++;
//		if (i % 45 == 0) {
//			try (var img = ScreenshotRecorder.takeScreenshot(altBuffer)) {
//				var f = new File("/tmp/stream.png");
//				img.writeFile(f);
//			} catch (IOException ignored) {
//
//			}
//		}

		// Render portals
		RenderSystem.disableBlend();

		var shaderProgram = SmunnelClient.PORTAL_SHADER.get();
		shaderProgram.bind();
		var interf = shaderProgram.getInterface();
		interf.projMat().set(RenderSystem.getProjectionMatrix());
		interf.modelViewMat().set(ctx.matrixStack().peek().getPosition());
		var w = MinecraftClient.getInstance().getWindow();
		interf.windowSize().set(w.getFramebufferWidth(), w.getFramebufferHeight());

		var tex = MainRenderManager.altBuffer.getColorAttachment();

		GL20C.glActiveTexture(GL20C.GL_TEXTURE0);
		GL20C.glBindTexture(GL20C.GL_TEXTURE_2D, tex);

		var x = ctx.camera().getPos().x;
		var y = ctx.camera().getPos().y;
		var z = ctx.camera().getPos().z;

		BufferBuilder bufferBuilder = Tessellator.getInstance().getBufferBuilder();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
		bufferBuilder.vertex(-1.0 - x,1.0 - y,0.0 - z).next();
		bufferBuilder.vertex( 2.0 - x,1.0 - y,0.0 - z).next();
		bufferBuilder.vertex( 2.0 - x,4.0 - y,0.0 - z).next();
		bufferBuilder.vertex(-1.0 - x,4.0 - y,0.0 - z).next();
		BufferRenderer.draw(bufferBuilder.end());

//		RenderSystem.disableBlend();
//		RenderSystem.defaultBlendFunc();
	}

	public static void swapToAlt() {
		RenderSystem.assertOnRenderThreadOrInit();
		assert originalBuffer == -2;
		originalBuffer = currentBuffer;
		altBuffer.beginWrite(false);
	}

	public static void swapToOriginal() {
		RenderSystem.assertOnRenderThreadOrInit();
		assert originalBuffer != -2;
		GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, originalBuffer); // Restore state
		originalBuffer = -2;
	}

	public static void setCurrentBuffer(int v) {
		currentBuffer = v;
	}
}
