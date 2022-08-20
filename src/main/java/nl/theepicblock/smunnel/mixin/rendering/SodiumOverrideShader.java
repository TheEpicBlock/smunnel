package nl.theepicblock.smunnel.mixin.rendering;

import me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader;
import net.minecraft.util.Identifier;
import nl.theepicblock.smunnel.SmunnelClient;
import nl.theepicblock.smunnel.rendering.ShaderPatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShaderLoader.class)
public class SodiumOverrideShader {
	@Inject(method = "getShaderSource", at = @At("RETURN"), cancellable = true)
	private static void getShaderSource(Identifier name, CallbackInfoReturnable<String> cir) {
		if (name.getPath().contains("blocks/block_layer_opaque.vsh")) {
			var src = cir.getReturnValue();
			cir.setReturnValue(ShaderPatcher.patchSodium(src));
		}
	}
}
