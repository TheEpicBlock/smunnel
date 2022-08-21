package nl.theepicblock.smunnel;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

// Quack
public interface WorldDuck {
	@NotNull
	TunnelHolder smunnel$getTunnels();

	static TunnelHolder get(ServerWorld world) {
		return ((WorldDuck)world).smunnel$getTunnels();
	}

	static TunnelHolder get(ClientWorld world) {
		return ((WorldDuck)world).smunnel$getTunnels();
	}

	static TunnelHolder get(Entity entity) {
		return ((WorldDuck)entity.getWorld()).smunnel$getTunnels();
	}
}
