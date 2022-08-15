package nl.theepicblock.smunnel;

import com.google.common.base.Suppliers;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.gl.shader.GlShader;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderType;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.util.Identifier;
import nl.theepicblock.smunnel.rendering.GlUniform2i;
import nl.theepicblock.smunnel.rendering.GlUniformMcMatrix4f;
import nl.theepicblock.smunnel.rendering.MainRenderManager;
import org.apache.commons.io.IOUtils;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class SmunnelClient implements ClientModInitializer {
	public static final Supplier<GlProgram<PortalShaderInterface>> PORTAL_SHADER = Suppliers.memoize(() -> new GlProgram.Builder(new Identifier("smunnel", "portal"))
			.attachShader(new GlShader(ShaderType.VERTEX,   new Identifier("smunnel", "portal_vert"), getShaderSrc("portal.vsh")))
			.attachShader(new GlShader(ShaderType.FRAGMENT, new Identifier("smunnel", "portal_frag"), getShaderSrc("portal.fsh")))
			.link(ctx -> new PortalShaderInterface(
					ctx.bindUniform("ModelViewMat", GlUniformMcMatrix4f::new),
					ctx.bindUniform("ProjMat", GlUniformMcMatrix4f::new),
					ctx.bindUniform("WindowSize", GlUniform2i::new)
				)
			));

	@Override
	public void onInitializeClient(ModContainer mod) {
		WorldRenderEvents.START.register(MainRenderManager::startRender);
		WorldRenderEvents.END.register(MainRenderManager::endRender);
	}

	private static String getShaderSrc(String name) {
		try (var stream = SmunnelClient.class.getResourceAsStream("/assets/smunnel/shaders/"+name)) {
			if (stream == null) {
				throw new IllegalStateException("Couldn't find shader src "+name);
			}
			return IOUtils.toString(stream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public record PortalShaderInterface(GlUniformMcMatrix4f modelViewMat, GlUniformMcMatrix4f projMat, GlUniform2i windowSize) {}
}
