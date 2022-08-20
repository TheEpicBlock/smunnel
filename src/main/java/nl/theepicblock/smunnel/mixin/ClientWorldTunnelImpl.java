package nl.theepicblock.smunnel.mixin;

import net.minecraft.client.world.ClientWorld;
import nl.theepicblock.smunnel.TunnelHolder;
import nl.theepicblock.smunnel.WorldDuck;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientWorld.class)
public class ClientWorldTunnelImpl implements WorldDuck {
	@Unique TunnelHolder tunnelHolder = new TunnelHolder();

	@Override
	public @NotNull TunnelHolder smunnel$getTunnels() {
		return tunnelHolder;
	}
}
