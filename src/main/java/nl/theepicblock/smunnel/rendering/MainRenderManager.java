package nl.theepicblock.smunnel.rendering;

import com.mojang.blaze3d.framebuffer.Framebuffer;
import com.mojang.blaze3d.framebuffer.SimpleFramebuffer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Direction;
import nl.theepicblock.smunnel.SmunnelClient;
import nl.theepicblock.smunnel.Tunnel;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.*;

public class MainRenderManager {
	public static Framebuffer altBuffer;
	private static int currentBuffer; // Stores the currently bound framebuffer
	private static int originalBuffer; // Temporarily stores a framebuffer whilst it's swapped to altBuffer
	private static int i = 0;
	private static boolean shouldRenderAlt = false;
	private static boolean shouldRenderInMain = false;
	private static SpaceCompressionShaderInterface.SpaceCompressionData shaderData = new SpaceCompressionShaderInterface.SpaceCompressionData(0, 0, 0);

	public static void startRender(WorldRenderContext ctx) {
		var t = getCurrentTunnel();
		if (t != null) {
			var c = ctx.camera().getPos().getComponentAlongAxis(t.axis());
			shouldRenderAlt = c < t.getMin() || c > t.getMax();
			shouldRenderInMain = t.isInTunnel(ctx.camera().getPos());
			shaderData = SpaceCompressionShaderInterface.getBasedOnTunnel(t, ctx.camera().getPos());
		}

		if (shouldRenderAlt()) {
			swapToAlt();
			altBuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
			swapToOriginal();
		}
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
		if (shouldRenderAlt()) {
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

			GL11.glEnable(GL32C.GL_DEPTH_CLAMP);
			BufferBuilder bufferBuilder = Tessellator.getInstance().getBufferBuilder();
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
			var t = getCurrentTunnel();
			bufferBuilder.vertex(t.xMin() - x, t.yMin() - y, t.zMax() - z).next();
			bufferBuilder.vertex(t.xMax() - x, t.yMin() - y, t.zMax() - z).next();
			bufferBuilder.vertex(t.xMax() - x, t.yMax() - y, t.zMax() - z).next();
			bufferBuilder.vertex(t.xMin() - x, t.yMax() - y, t.zMax() - z).next();
			BufferRenderer.draw(bufferBuilder.end());

			bufferBuilder = Tessellator.getInstance().getBufferBuilder();
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
			bufferBuilder.vertex(t.xMax() - x, t.yMin() - y, t.zMin() - z).next();
			bufferBuilder.vertex(t.xMin() - x, t.yMin() - y, t.zMin() - z).next();
			bufferBuilder.vertex(t.xMin() - x, t.yMax() - y, t.zMin() - z).next();
			bufferBuilder.vertex(t.xMax() - x, t.yMax() - y, t.zMin() - z).next();
			BufferRenderer.draw(bufferBuilder.end());
			GL11.glDisable(GL32C.GL_DEPTH_CLAMP);
		}
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

	public static boolean shouldRenderAlt() {
		return shouldRenderAlt;
	}

	public static boolean shouldRenderInMain() {
		return shouldRenderInMain;
	}

	public static SpaceCompressionShaderInterface.SpaceCompressionData getShaderData() {
		return shaderData;
	}

	@Nullable
	public static Tunnel getCurrentTunnel() {
		return new Tunnel(
				-80,0,
				1, 4,
				-1, 2,
				Direction.Axis.Z,
				1f
		);
	}
}
