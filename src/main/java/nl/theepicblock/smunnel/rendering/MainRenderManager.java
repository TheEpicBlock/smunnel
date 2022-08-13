package nl.theepicblock.smunnel.rendering;

import com.mojang.blaze3d.framebuffer.Framebuffer;
import com.mojang.blaze3d.framebuffer.SimpleFramebuffer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import org.lwjgl.opengl.GL30;

import java.io.File;
import java.io.IOException;

public class MainRenderManager {
	public static Framebuffer altBuffer;
	private static int currentBuffer; // Stores the currently bound framebuffer
	private static int originalBuffer; // Temporarily stores a framebuffer whilst it's swapped to altBuffer
	private static int i = 0;

	public static void startRender() {
//		var w = MinecraftClient.getInstance().getWindow();
//		altBuffer = new SimpleFramebuffer(w.getFramebufferWidth(), w.getFramebufferHeight(), true, MinecraftClient.IS_SYSTEM_MAC);
	}

	static {
		var w = MinecraftClient.getInstance().getWindow();
		altBuffer = new SimpleFramebuffer(w.getFramebufferWidth(), w.getFramebufferHeight(), true, MinecraftClient.IS_SYSTEM_MAC);
	}

	public static void endRender() {
		i++;
		if (i % 15 == 0) {
			try (var img = ScreenshotRecorder.takeScreenshot(altBuffer)) {
				var f = new File("/tmp/stream.png");
				img.writeFile(f);
			} catch (IOException ignored) {

			}
		}
		altBuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
//		altBuffer.delete();
//		altBuffer = null;
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
