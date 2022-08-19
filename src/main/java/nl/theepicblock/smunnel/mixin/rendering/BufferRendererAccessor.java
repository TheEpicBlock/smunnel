package nl.theepicblock.smunnel.mixin.rendering;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferRenderer;
import com.mojang.blaze3d.vertex.VertexBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BufferRenderer.class)
public interface BufferRendererAccessor {
	@Invoker
	static VertexBuffer callUpload(BufferBuilder.RenderedBuffer renderedBuffer) {
		throw new UnsupportedOperationException();
	}
}
