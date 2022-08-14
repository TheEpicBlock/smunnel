package nl.theepicblock.smunnel.rendering;

import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniform;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL20C;

public class GlUniform2i extends GlUniform<Vector2i> {
	public GlUniform2i(int index) {
		super(index);
	}

	@Override
	public void set(Vector2i vector2i) {
		set(vector2i.x, vector2i.y);
	}

	public void set(int x, int y) {
		GL20C.glUniform2i(this.index, x, y);
	}
}
