package nl.theepicblock.smunnel;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public record Tunnel(
		int zMin,
		int zMax,
		int yMin,
		int yMax,
		int xMin,
		int xMax,
		Direction.Axis axis,
		int targetLength
) {
	public boolean isInTunnel(Vec3d vec3d) {
		return vec3d.getZ() > zMin && vec3d.getZ() < zMax &&
				vec3d.getX() > xMin && vec3d.getX() < xMax &&
				vec3d.getY() > yMin && vec3d.getY() < yMax;
	}

	public int getMin() {
		return this.axis().choose(xMin, yMin, zMin);
	}

	public int getMax() {
		return this.axis().choose(xMax, yMax, zMax);
	}
}
