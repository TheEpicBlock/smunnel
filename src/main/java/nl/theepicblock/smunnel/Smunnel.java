package nl.theepicblock.smunnel;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import nl.theepicblock.smunnel.rendering.MainRenderManager;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.resource.loader.api.client.ClientResourceLoaderEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Smunnel implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("smunnel");

	@Override
	public void onInitialize(ModContainer mod) {
		WorldRenderEvents.START.register(ctx -> MainRenderManager.startRender());
		WorldRenderEvents.END.register(ctx -> MainRenderManager.endRender());

	}
}
