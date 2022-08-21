package nl.theepicblock.smunnel.rendering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;
import nl.theepicblock.smunnel.Tunnel;
import org.lwjgl.opengl.GL20C;

@Environment(EnvType.CLIENT)
public class SpaceCompressionShaderInterface {
	private final int tunnelStartIndex;
	private final int tunnelDataIndex;
	private final int tunnelMultiplicationFactorIndex;

	public SpaceCompressionShaderInterface(int shaderHandle) {
		this.tunnelStartIndex = getIndex(shaderHandle, "tunnelStart");
		this.tunnelDataIndex = getIndex(shaderHandle, "tunnelData");
		this.tunnelMultiplicationFactorIndex = getIndex(shaderHandle, "tunnelMultiplicationFactor");
	}

	private static int getIndex(int shaderHandle, String name) {
		int index = GL20C.glGetUniformLocation(shaderHandle, name);
		if (index < 0) {
			throw new NullPointerException("No uniform exists with name: " + name);
		} else {
			return index;
		}
	}

	public void setDisabled() {
		GL20C.glUniform1i(tunnelDataIndex, 0);
	}

	public void setEnabled(SpaceCompressionData data) {
		GL20C.glUniform1i(tunnelDataIndex, data.tunnelData());
	}

	public void init(SpaceCompressionData data) {
		GL20C.glUniform1f(tunnelStartIndex, data.tunnelStart());
//		GL20C.glUniform1i(tunnelDataIndex, data.tunnelData()); // Set dynamically to enable / disable shader
		GL20C.glUniform1f(tunnelMultiplicationFactorIndex, data.tunnelMultiplicationFactor());
	}

	public static SpaceCompressionData getBasedOnTunnel(Tunnel t, Vec3d cameraPos) {
		int boundMode = 0;

		var cameraCoordinate = cameraPos.getComponentAlongAxis(t.axis());
		var tMin = t.getMin() - cameraCoordinate;
		var tMax = t.getMax() - cameraCoordinate;
		int tunnelLength = t.getMax()-t.getMin();

		if (tMin < 0 && tMax > 0) {
			boundMode = 0b11;
		} else if (tMin > 0) {
			boundMode = 0b01;
		} else {
			boundMode = 0b10;
//			tunnelLength *= -1;
		}

		int axis = switch (t.axis()) {
			case X -> 0b01;
			case Y -> 0b10;
			case Z -> 0b11;
		};

		float tunnelStart = (float)tMin;

		return new SpaceCompressionData(
				tunnelStart,
				(boundMode << 30) | (axis << 28) | (tunnelLength + 134217727),
				Math.abs((float)t.targetLength() / tunnelLength)
		);
	}

	record SpaceCompressionData(float tunnelStart, int tunnelData, float tunnelMultiplicationFactor) {}
}
