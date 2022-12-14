package nl.theepicblock.smunnel.rendering;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.framebuffer.Framebuffer;
import com.mojang.blaze3d.framebuffer.SimpleFramebuffer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.gl.shader.GlShader;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import nl.theepicblock.smunnel.ListUtil;
import nl.theepicblock.smunnel.Tunnel;
import nl.theepicblock.smunnel.WorldDuck;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32C;

import java.util.ArrayList;
import java.util.function.Supplier;

import static nl.theepicblock.smunnel.SmunnelClient.getShaderSrc;

@Environment(EnvType.CLIENT)
public class MainRenderManager {
	public static final Supplier<GlProgram<PortalShaderInterface>> PORTAL_SHADER = Suppliers.memoize(() -> new GlProgram.Builder(new Identifier("smunnel", "portal"))
			.attachShader(new GlShader(ShaderType.VERTEX,   new Identifier("smunnel", "portal_vert"), getShaderSrc("portal.vsh")))
			.attachShader(new GlShader(ShaderType.FRAGMENT, new Identifier("smunnel", "portal_frag"), getShaderSrc("portal.fsh")))
			.link(ctx -> new PortalShaderInterface(
							ctx.bindUniform("ModelViewMat", GlUniformMcMatrix4f::new),
							ctx.bindUniform("ProjMat", GlUniformMcMatrix4f::new),
							ctx.bindUniform("WindowSize", GlUniform2i::new)
					)
			));

	private static int currentBuffer; // Stores the currently bound framebuffer

	// State
	private static final ArrayList<Framebuffer> altFramebuffers = new ArrayList<>();
	private static final ArrayList<Tunnel> activeTunnels = new ArrayList<>();
	private static final ArrayList<SpaceCompressionShaderInterface.SpaceCompressionData> shaderData = new ArrayList<>();
	private static SpaceCompressionShaderInterface.SpaceCompressionData mainShaderData = null; // shader data for the main shader (if any)

	public static void startRender(WorldRenderContext ctx) {
		for (var buffer : altFramebuffers) {
			executeWithBuffer(buffer, () -> buffer.clear(MinecraftClient.IS_SYSTEM_MAC));
		}
	}

	public static void setupRender(WorldRenderContext ctx) {
		var holder = WorldDuck.get(ctx.world());
		var cameraPos = ctx.camera().getPos();

		shaderData.clear();
		activeTunnels.clear();
		mainShaderData = null;

		for (var tunnel : holder.tunnels) {
			var c = cameraPos.getComponentAlongAxis(tunnel.axis());
			var frustrum = ctx.frustum();

			if (tunnel.isInTunnel(cameraPos)) {
				mainShaderData = SpaceCompressionShaderInterface.getBasedOnTunnel(tunnel, cameraPos);
				continue;
			}

			var shouldRender = c < tunnel.getMin() || c > tunnel.getMax();
			if (frustrum != null) {
				shouldRender &= frustrum.isVisible(tunnel.getBoxForCulling(cameraPos));
			}

			if (shouldRender) {
				shaderData.add(SpaceCompressionShaderInterface.getBasedOnTunnel(tunnel, cameraPos));
				activeTunnels.add(tunnel);
			}
		}

		ListUtil.setSize(
				altFramebuffers,
				shaderData.size(),
				buffer -> executeWithBuffer(buffer, buffer::delete),
				MainRenderManager::newAltbuffer
		);
	}

	private static Framebuffer newAltbuffer() {
		var originalBuffer = currentBuffer;

		var w = MinecraftClient.getInstance().getWindow();
		var altBuffer = new SimpleFramebuffer(w.getFramebufferWidth(), w.getFramebufferHeight(), true, MinecraftClient.IS_SYSTEM_MAC);
		altBuffer.setClearColor(0,0,0,0);

		restoreFramebuffer(originalBuffer); // Just in case
		return altBuffer;
	}

	private static void restoreFramebuffer(int b) {
		GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, b);
	}

	public static void endRender(WorldRenderContext ctx) {
		// Render portals
		if (activeTunnels.isEmpty()) return;

		var w = MinecraftClient.getInstance().getWindow();

		var depth = GL20C.glGetInteger(GL20C.GL_DEPTH_TEST);
		var blend = GL20C.glGetInteger(GL20C.GL_BLEND);

		var activeTexture = GlStateManager._getActiveTexture();
		GlStateManager._activeTexture(GL20C.GL_TEXTURE0);

		RenderSystem.disableBlend();
		RenderSystem.enableDepthTest();
		GL11.glEnable(GL32C.GL_DEPTH_CLAMP);

		var shaderProgram = PORTAL_SHADER.get();
		shaderProgram.bind();
		var shaderInterface = shaderProgram.getInterface();
		shaderInterface.projMat().set(RenderSystem.getProjectionMatrix());
		shaderInterface.modelViewMat().set(ctx.matrixStack().peek().getModel());
		shaderInterface.windowSize().set(w.getFramebufferWidth(), w.getFramebufferHeight());

		for (var i = 0; i < activeTunnels.size(); i++) {
			var t = activeTunnels.get(i);
			var framebuffer = altFramebuffers.get(i);

			// TODO do this properly
			framebuffer.bindColorAttachmentAsTexture();
			t.render(ctx.camera().getPos());
		}
		GL11.glDisable(GL32C.GL_DEPTH_CLAMP);

		if (depth != 0) {
			RenderSystem.enableDepthTest();
		} else {
			RenderSystem.disableDepthTest();
		}
		if (blend != 0) {
			RenderSystem.enableBlend();
		} else {
			RenderSystem.disableBlend();
		}

		GlStateManager._activeTexture(activeTexture);
	}

	public static void onResolutionChanged(int width, int height) {
		altFramebuffers.forEach(buffer -> buffer.resize(width, height, MinecraftClient.IS_SYSTEM_MAC));
	}

	public static void executeWithBuffer(Framebuffer buffer, Runnable r) {
		RenderSystem.assertOnRenderThreadOrInit();
		var originalBuffer = currentBuffer;
		buffer.beginWrite(false);
		r.run();
		restoreFramebuffer(originalBuffer);
	}

	public static void executeAlts(Runnable r) {
		var originalBuffer = currentBuffer;
		for (var buffer : altFramebuffers) {
			buffer.beginWrite(false);
			r.run();
		}
		restoreFramebuffer(originalBuffer);
	}

	public static void executeAltsWithShader(SpaceCompressionShaderInterface shaderInterface, Runnable r) {
		var originalBuffer = currentBuffer;
		for (var i = 0; i < shaderData.size(); i++) {
			shaderInterface.setEnabled(shaderData.get(i));
			altFramebuffers.get(i).beginWrite(false);
			r.run();
		}
		shaderInterface.setDisabled();
		restoreFramebuffer(originalBuffer);
	}

	public static void executeMainWithShader(SpaceCompressionShaderInterface shaderInterface, Runnable r) {
		var enable = mainShaderData != null;
		if (enable) shaderInterface.setEnabled(mainShaderData);
		r.run();
		if (enable) shaderInterface.setDisabled();
	}

	public static void setCurrentBuffer(int v) {
		currentBuffer = v;
	}

	public record PortalShaderInterface(GlUniformMcMatrix4f modelViewMat, GlUniformMcMatrix4f projMat, GlUniform2i windowSize) {}
}
