package nl.theepicblock.smunnel.mixin.rendering;

import com.mojang.blaze3d.glfw.Window;
import net.minecraft.client.MinecraftClient;
import nl.theepicblock.smunnel.rendering.MainRenderManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ResolutionChange {
	@Shadow
	@Final
	private Window window;

	@Inject(method = "onResolutionChanged", at = @At("RETURN"))
	private void onResolutionChanged(CallbackInfo ci) {
		MainRenderManager.altBuffer.resize(this.window.getFramebufferWidth(), this.window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
	}
}
