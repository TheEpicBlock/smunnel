package nl.theepicblock.smunnel;

import com.mojang.blaze3d.vertex.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static java.lang.Math.max;
import static java.lang.Math.min;

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
			var clampedPartInTunnel = clamp(partInTunnel, -tunnelLength, tunnelLength);
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
				inTunnel = clamp(coordinate - tunnelStart, 0, tunnelLength);
				postTunnel = max(0, coordinate - tunnelEnd);
			} else {
				preTunnel = max(coordinate, tunnelEnd);
				inTunnel = clamp(coordinate - tunnelEnd, -tunnelLength, 0);
				postTunnel = min(0, coordinate - tunnelStart);
			}


			var multiplicationFactor = Math.abs(targetLength() / tunnelLength);
			return ray.withAxis(this.axis(), preTunnel + (inTunnel * multiplicationFactor) + postTunnel);
		} else {
			return ray;
		}
	}

	public Box getBoxForCulling(Vec3d cameraPos) {
		var c = cameraPos.getComponentAlongAxis(this.axis());
		var b = c > this.getMax();

		var minVec = new Vec3d(xMin(), yMin(), zMin());
		var maxVec = new Vec3d(xMax(), yMax(), zMax());

		if (b) {
			minVec = minVec.withAxis(this.axis(), this.getMax());
			maxVec = maxVec.withAxis(this.axis(), this.getMax());
		} else {
			minVec = minVec.withAxis(this.axis(), this.getMin());
			maxVec = maxVec.withAxis(this.axis(), this.getMin());
		}

		return new Box(minVec, maxVec);
	}

	@Environment(EnvType.CLIENT)
	public void render(Vec3d cameraPos) {
		var c = cameraPos.getComponentAlongAxis(this.axis());
		var b = c > this.getMax();

		var xS = this.xMin() - cameraPos.x;
		var yS = this.yMin() - cameraPos.y;
		var zS = this.zMin() - cameraPos.z;
		var xM = this.xMax() - cameraPos.x;
		var yM = this.yMax() - cameraPos.y;
		var zM = this.zMax() - cameraPos.z;

		BufferBuilder bufferBuilder = Tessellator.getInstance().getBufferBuilder();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

		switch (this.axis()) {
			case X -> {
				if (b) {
					bufferBuilder.vertex(xM, yS, zS).next();
					bufferBuilder.vertex(xM, yM, zS).next();
					bufferBuilder.vertex(xM, yM, zM).next();
					bufferBuilder.vertex(xM, yS, zM).next();
				} else {
					bufferBuilder.vertex(xS, yS, zS).next();
					bufferBuilder.vertex(xS, yS, zM).next();
					bufferBuilder.vertex(xS, yM, zM).next();
					bufferBuilder.vertex(xS, yM, zS).next();
				}
			}
			case Y -> {
				if (b) {
					bufferBuilder.vertex(xS, yM, zS).next();
					bufferBuilder.vertex(xS, yM, zM).next();
					bufferBuilder.vertex(xM, yM, zM).next();
					bufferBuilder.vertex(xM, yM, zS).next();
				} else {
					bufferBuilder.vertex(xS, yS, zS).next();
					bufferBuilder.vertex(xM, yS, zS).next();
					bufferBuilder.vertex(xM, yS, zM).next();
					bufferBuilder.vertex(xS, yS, zM).next();
				}
			}
			case Z -> {
				if (b) {
					bufferBuilder.vertex(xS, yS, zM).next();
					bufferBuilder.vertex(xM, yS, zM).next();
					bufferBuilder.vertex(xM, yM, zM).next();
					bufferBuilder.vertex(xS, yM, zM).next();
				} else {
					bufferBuilder.vertex(xM, yS, zS).next();
					bufferBuilder.vertex(xS, yS, zS).next();
					bufferBuilder.vertex(xS, yM, zS).next();
					bufferBuilder.vertex(xM, yM, zS).next();
				}
			}
		}

		BufferRenderer.draw(bufferBuilder.end());
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

	public static double clamp(double val, double a, double b) {
		return max(a,min(b,val));
	}
}
