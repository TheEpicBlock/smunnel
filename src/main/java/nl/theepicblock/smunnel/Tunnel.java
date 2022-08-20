package nl.theepicblock.smunnel;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.joml.Math.clamp;

public record Tunnel(
		int zMin,
		int zMax,
		int yMin,
		int yMax,
		int xMin,
		int xMax,
		Direction.Axis axis,
		float targetLength
) {
	public boolean isInTunnel(Vec3d vec3d) {
		return vec3d.getZ() >= zMin && vec3d.getZ() <= zMax &&
				vec3d.getX() >= xMin && vec3d.getX() <= xMax &&
				vec3d.getY() >= yMin && vec3d.getY() <= yMax;
	}

	public boolean isInTunnelExtended(Vec3d vec3d) {
		return switch (this.axis()) {
			case X -> vec3d.getZ() >= zMin && vec3d.getZ() <= zMax &&
					vec3d.getY() >= yMin && vec3d.getY() <= yMax;
			case Y -> vec3d.getZ() >= zMin && vec3d.getZ() <= zMax &&
					vec3d.getX() >= xMin && vec3d.getX() <= xMax;
			case Z -> vec3d.getX() >= xMin && vec3d.getX() <= xMax &&
					vec3d.getY() >= yMin && vec3d.getY() <= yMax;
		};
	}

	public int getMin() {
		return this.axis().choose(xMin, yMin, zMin);
	}

	public int getMax() {
		return this.axis().choose(xMax, yMax, zMax);
	}

	public Vec3d rayToWorldSpace(Vec3d source, Vec3d ray) {
		if (isInTunnelExtended(source)) {
			// THIS CODE IS MESS AAAAAAAAa
			var coordinate = ray.getComponentAlongAxis(this.axis());
			var sourceCoord = source.getComponentAlongAxis(this.axis());
			var tunnelStart = this.getMin() - sourceCoord;
			var tunnelEnd = this.getMax() - sourceCoord;

			if (tunnelStart < 0 && tunnelEnd > 0) {
				if (coordinate > 0) {
					tunnelStart = 0;
				} else {
					tunnelEnd = 0;
				}
			}
			double partInTunnel;
			if (coordinate > 0) {
				if (tunnelEnd < 0) return ray;
				partInTunnel = max(0, coordinate - tunnelStart);
			} else {
				if (tunnelStart > 0) return ray;
				partInTunnel = min(0, coordinate - tunnelEnd);
			}
			var partOutTunnel = coordinate - partInTunnel;
			var tunnelLength = tunnelEnd - tunnelStart;
			var multiplicationFactor = Math.abs((float)(this.getMax()-this.getMin()) / targetLength());
			partInTunnel *= multiplicationFactor;
			var clampedPartInTunnel = clamp(-tunnelLength, tunnelLength, partInTunnel);
			var diff = (partInTunnel - clampedPartInTunnel) / multiplicationFactor;

			return ray.withAxis(this.axis(), partOutTunnel + clampedPartInTunnel + diff);
		} else {
			return ray;
		}
	}

	public Vec3d rayToIllusionSpace(Vec3d source, Vec3d ray) {
		if (isInTunnelExtended(source)) {
			// THIS CODE IS MESS AAAAAAAAa
			var coordinate = ray.getComponentAlongAxis(this.axis());
			var sourceCoord = source.getComponentAlongAxis(this.axis());
			var tunnelStart = this.getMin() - sourceCoord;
			var tunnelEnd = this.getMax() - sourceCoord;
			var tunnelLength = tunnelEnd - tunnelStart;

			double preTunnel;
			double inTunnel;
			double postTunnel;
			if (tunnelStart < 0 && tunnelEnd > 0) {
				preTunnel = 0;
				if (coordinate > 0) {
					inTunnel = min(tunnelEnd, coordinate);
					postTunnel = max(0, coordinate - tunnelEnd);
				} else {
					inTunnel = max(tunnelStart, coordinate);
					postTunnel = min(0, coordinate - tunnelStart);
				}
			} else if (tunnelStart > 0) {
				preTunnel = min(coordinate, tunnelStart);
				inTunnel = clamp(0, tunnelLength, coordinate - tunnelStart);
				postTunnel = max(0, coordinate - tunnelEnd);
			} else {
				preTunnel = max(coordinate, tunnelEnd);
				inTunnel = clamp(-tunnelLength, 0, coordinate - tunnelEnd);
				postTunnel = min(0, coordinate - tunnelStart);
			}


			var multiplicationFactor = Math.abs(targetLength() / tunnelLength);
			return ray.withAxis(this.axis(), preTunnel + (inTunnel * multiplicationFactor) + postTunnel);
		} else {
			return ray;
		}
	}

	public static Tunnel fromPacket(PacketByteBuf buf) {
		return new Tunnel(
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readEnumConstant(Direction.Axis.class),
				buf.readFloat()
		);
	}

	public void writePacket(PacketByteBuf buf) {
		buf.writeVarInt(this.zMin());
		buf.writeVarInt(this.zMax());
		buf.writeVarInt(this.yMin());
		buf.writeVarInt(this.yMax());
		buf.writeVarInt(this.xMin());
		buf.writeVarInt(this.xMax());
		buf.writeEnumConstant(this.axis());
		buf.writeFloat(this.targetLength());
	}
}
