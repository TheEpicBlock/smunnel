package nl.theepicblock.smunnel.mixin;

import com.mojang.blaze3d.framebuffer.Framebuffer;
import com.mojang.blaze3d.platform.GlStateManager;
import nl.theepicblock.smunnel.rendering.MainRenderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GlStateManager.class, remap = false)
public class TrackFrameBuffer {
	@Inject(method = "_glBindFramebuffer", at = @At("RETURN"))
	private static void onBind(int i, int j, CallbackInfo ci) {
		MainRenderManager.setCurrentBuffer(j);
	}
}
