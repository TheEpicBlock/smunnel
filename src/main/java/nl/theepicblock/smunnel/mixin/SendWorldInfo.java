package nl.theepicblock.smunnel.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import nl.theepicblock.smunnel.Smunnel;
import nl.theepicblock.smunnel.WorldDuck;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerManager.class)
public class SendWorldInfo {
	@Inject(method = "sendWorldInfo", at = @At("RETURN"))
	private void onSyncWorldInfo(ServerPlayerEntity player, ServerWorld world, CallbackInfo ci) {
		var buf = PacketByteBufs.create();
		WorldDuck.get(world).writeToBuf(buf);
		ServerPlayNetworking.send(player, Smunnel.SYNC_PACKET, buf);
	}
}
