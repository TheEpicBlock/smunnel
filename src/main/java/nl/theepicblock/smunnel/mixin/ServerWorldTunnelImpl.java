package nl.theepicblock.smunnel.mixin;

import net.minecraft.server.world.ServerWorld;
import nl.theepicblock.smunnel.TunnelHolder;
import nl.theepicblock.smunnel.WorldDuck;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(ServerWorld.class)
public class ServerWorldTunnelImpl implements WorldDuck {
	@Override
	public @NotNull TunnelHolder smunnel$getTunnels() {
		return TunnelHolder.getFromPersistentState(((ServerWorld)(Object)this));
	}
}
