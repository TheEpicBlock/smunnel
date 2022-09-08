package nl.theepicblock.smunnel;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import nl.theepicblock.smunnel.rendering.MainRenderManager;
import org.apache.commons.io.IOUtils;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SmunnelClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		WorldRenderEvents.START.register(MainRenderManager::startRender);
		WorldRenderEvents.AFTER_SETUP.register(MainRenderManager::setupRender);
		WorldRenderEvents.LAST.register(MainRenderManager::endRender);

		// Networking
		ClientPlayNetworking.registerGlobalReceiver(Smunnel.SYNC_PACKET, (client, handler, buf, responseSender) -> {
			var packet = TunnelHolder.readPacket(buf);
			client.execute(() -> {
				if (client.world == null) {
					Smunnel.LOGGER.error("Received portal info packet but the world is null");
					return;
				}
				((WorldDuck)client.world).smunnel$getTunnels().importFromPacket(packet);
			});
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
