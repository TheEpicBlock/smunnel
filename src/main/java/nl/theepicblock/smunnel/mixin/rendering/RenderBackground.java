package nl.theepicblock.smunnel.mixin.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tessellator;
import net.minecraft.client.render.BackgroundRenderer;
import nl.theepicblock.smunnel.rendering.MainRenderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BackgroundRenderer.class)
public class RenderBackground {
	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clearColor(FFFF)V"))
	private static void redirectClearColor(float f, float g, float h, float i) {
		RenderSystem.clearColor(f, g, h, i);

		MainRenderManager.executeAlts(() -> RenderSystem.clearColor(f, g, h, i));
	}
}
