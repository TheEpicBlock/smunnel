package nl.theepicblock.smunnel.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import nl.theepicblock.smunnel.WorldDuck;
import nl.theepicblock.smunnel.rendering.MainRenderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@SuppressWarnings("ConstantConditions")
@Mixin(Entity.class)
public abstract class MovementImpl {
	@Shadow
	public abstract Vec3d getPos();

	@Unique double smunnel$prevX;
	@Unique double smunnel$prevY;
	@Unique double smunnel$prevZ;

	@ModifyVariable(method = "move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"), argsOnly = true)
	Vec3d modifyMovement(Vec3d in) {
		if (((Object)this) instanceof ServerPlayerEntity) return in;
		this.smunnel$prevX = this.getPos().getX();
		this.smunnel$prevY = this.getPos().getY();
		this.smunnel$prevZ = this.getPos().getZ();
		// We interpret the ray as if it were in illusion space
		return WorldDuck.get((Entity)(Object)this).rayToWorldSpace(this.getPos(), in);
	}

	@ModifyVariable(method = "move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V", index = 3, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;fall(DZLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V"))
	Vec3d modifyMovement2(Vec3d in) {
		// Past this point we should use illusion space again
		if (((Object)this) instanceof ServerPlayerEntity) return in;
		// We interpret the ray as if it were in illusion space
		return WorldDuck.get((Entity)(Object)this).rayToIllusionSpace(new Vec3d(this.smunnel$prevX, this.smunnel$prevY, this.smunnel$prevZ), in);
	}
}
