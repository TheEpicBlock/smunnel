package nl.theepicblock.smunnel.mixin;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import nl.theepicblock.smunnel.WorldDuck;
import nl.theepicblock.smunnel.rendering.MainRenderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class FixMovedTooFast {
	@Shadow
	public ServerPlayerEntity player;

	@Shadow
	private double lastTickX;

	@Shadow
	private double lastTickZ;

	@Shadow
	private double lastTickY;

	@ModifyVariable(method = "onPlayerMove", index = 27, at = @At(value = "LOAD", ordinal = 0))
	double redirectVelocity(double original, PlayerMoveC2SPacket packet) {
		var playerPos = this.player.getPos();
		var moveX = packet.getX(playerPos.x) - this.lastTickX;
		var moveY = packet.getY(playerPos.y) - this.lastTickY;
		var moveZ = packet.getZ(playerPos.z) - this.lastTickZ;
		return WorldDuck.get(this.player.getWorld()).rayToIllusionSpace(this.player.getPos(), new Vec3d(moveX, moveY, moveZ)).lengthSquared();
	}
}
