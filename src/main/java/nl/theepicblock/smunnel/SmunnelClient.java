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
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class SmunnelClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		WorldRenderEvents.START.register(MainRenderManager::startRender);
		WorldRenderEvents.END.register(MainRenderManager::endRender);

		// Networking
		ClientPlayNetworking.registerGlobalReceiver(Smunnel.SYNC_PACKET, (client, handler, buf, responseSender) -> {
			((WorldDuck)client.world).smunnel$getTunnels().importFromPacket(buf);
		});
	}

	public static String getShaderSrc(String name) {
		try (var stream = SmunnelClient.class.getResourceAsStream("/assets/smunnel/shaders/"+name)) {
			if (stream == null) {
				throw new IllegalStateException("Couldn't find shader src "+name);
			}
			return IOUtils.toString(stream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
