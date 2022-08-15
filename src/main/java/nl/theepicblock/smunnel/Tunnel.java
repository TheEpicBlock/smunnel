package nl.theepicblock.smunnel;

import net.minecraft.util.math.Vec3d;

public record Tunnel(
		int start,
		int end,
		int yMin,
		int yMax,
		int xMin,
		int xMax
) {
	public boolean isInTunnel(Vec3d vec3d) {
		return vec3d.getZ() > start && vec3d.getZ() < end &&
				vec3d.getX() > xMin && vec3d.getX() < xMax &&
				vec3d.getY() > yMin && vec3d.getY() < yMax;
	}

}
