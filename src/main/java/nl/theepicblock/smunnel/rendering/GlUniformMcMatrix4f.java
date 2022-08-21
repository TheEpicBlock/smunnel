package nl.theepicblock.smunnel.rendering;

import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

/**
 * The same as {@link me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformMatrix4f} but using Mc's matrices
 */
@Environment(EnvType.CLIENT)
public class GlUniformMcMatrix4f extends GlUniform<Matrix4f> {
	public GlUniformMcMatrix4f(int index) {
		super(index);
	}

	@Override
	public void set(Matrix4f matrix4f) {
		MemoryStack stack = MemoryStack.stackPush();

		try {
			FloatBuffer buf = stack.callocFloat(16);
			matrix4f.writeColumnMajor(buf);
			GL30C.glUniformMatrix4fv(this.index, false, buf);
		} catch (Throwable var6) {
			if (stack != null) {
				try {
					stack.close();
				} catch (Throwable var5) {
					var6.addSuppressed(var5);
				}
			}

			throw var6;
		}

		if (stack != null) {
			stack.close();
		}
	}
}
