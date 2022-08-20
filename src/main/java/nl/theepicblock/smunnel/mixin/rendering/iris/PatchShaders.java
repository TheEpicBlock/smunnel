package nl.theepicblock.smunnel.mixin.rendering.iris;

import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.newshader.ShaderAttributeInputs;
import net.coderbot.iris.pipeline.newshader.TriforcePatcher;
import net.minecraft.util.Identifier;
import nl.theepicblock.smunnel.rendering.ShaderPatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TriforcePatcher.class, remap = false)
public class PatchShaders {
	@Inject(method = "patchSodium", at = @At("RETURN"), cancellable = true)
	private static void smunnel$pathSodium(String par1, ShaderType par2, AlphaTest par3, ShaderAttributeInputs par4, float par5, float par6, float par7, CallbackInfoReturnable<String> cir) {
		if (par2 == ShaderType.VERTEX) {
			var src = cir.getReturnValue();
			cir.setReturnValue(ShaderPatcher.patchIris(src));
		}
	}

	@Inject(method = "patchVanilla", at = @At("RETURN"), cancellable = true)
	private static void smunnel$patchVanilla(String par1, ShaderType par2, AlphaTest par3, boolean par4, ShaderAttributeInputs par5, boolean par6, CallbackInfoReturnable<String> cir) {
		if (par2 == ShaderType.VERTEX) {
			var src = cir.getReturnValue();
			cir.setReturnValue(ShaderPatcher.patchIris(src));
		}
	}
}
