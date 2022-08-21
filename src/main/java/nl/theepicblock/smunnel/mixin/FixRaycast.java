package nl.theepicblock.smunnel.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import nl.theepicblock.smunnel.WorldDuck;
import nl.theepicblock.smunnel.rendering.MainRenderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.joml.Math.clamp;

@Mixin(Entity.class)
public abstract class FixRaycast {

	@Shadow
	public abstract Vec3d getCameraPosVec(float tickDelta);

	@Shadow
	public abstract Vec3d getRotationVec(float tickDelta);

	@Shadow
	public World world;

	/**
	 * @author TheEpicBlock
	 * @reason I needed a quick way to implement proper raycasting through tunnels, don't judge me
	 */
	@Overwrite
	public HitResult raycast(double maxDistance, float tickDelta, boolean includeFluids) {
		Vec3d source = this.getCameraPosVec(tickDelta);
		Vec3d rotation = this.getRotationVec(tickDelta);

		var tunnels = WorldDuck.get((Entity)(Object)this);

		// I don't have the time to do this all mathy and properly, so I will just split the ray in 100 parts
		var part = maxDistance / 100;
		int i = 0;
		while(true) {
			var vec1 = tunnels.rayToWorldSpace(source, rotation.multiply(part*i));
			var vec2 = tunnels.rayToWorldSpace(source, rotation.multiply(part*(i+1)));
			var raycast = this.world.raycast(new RaycastContext(source.add(vec1), source.add(vec2), RaycastContext.ShapeType.OUTLINE, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE,  ((Entity)(Object)this)));;
			if (raycast.getType() != HitResult.Type.MISS || i == 99) return raycast;
			i++;
		}
	}
}
