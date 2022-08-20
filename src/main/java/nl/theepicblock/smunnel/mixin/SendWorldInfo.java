package nl.theepicblock.smunnel.mixin;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class SendWorldInfo {
	@Inject(method = "sendWorldInfo", at = @At("HEAD"))
	private void onSyncWorldInfo(ServerPlayerEntity player, ServerWorld world, CallbackInfo ci) {

	}
}
