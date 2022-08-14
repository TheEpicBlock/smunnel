package nl.theepicblock.smunnel.mixin;

import me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShaderLoader.class)
public class SodiumOverrideShader {
	@Inject(method = "getShaderSource", at = @At("HEAD"), cancellable = true)
	private static void getShaderSource(Identifier name, CallbackInfoReturnable<String> cir) {
		if (name.getPath().contains("blocks/block_layer_opaque.fsh")) {
			cir.setReturnValue("""
#version 150 core

#import <sodium:include/fog.glsl>

in vec4 v_Color; // The interpolated vertex color
in vec2 v_TexCoord; // The interpolated block texture coordinates
in vec2 v_LightCoord; // The interpolated light map texture coordinates
in float v_FragDistance; // The fragment's distance from the camera

uniform sampler2D u_BlockTex; // The block texture sampler
uniform sampler2D u_LightTex; // The light map texture sampler

uniform vec4 u_FogColor; // The color of the shader fog
uniform float u_FogStart; // The starting position of the shader fog
uniform float u_FogEnd; // The ending position of the shader fog

out vec4 fragColor; // The output fragment for the color framebuffer

void main() {
    vec4 sampleBlockTex = texture(u_BlockTex, v_TexCoord);

#ifdef ALPHA_CUTOFF
    if (sampleBlockTex.a < ALPHA_CUTOFF) {
        discard;
    }
#endif

    vec4 sampleLightTex = texture(u_LightTex, v_LightCoord);

    vec4 diffuseColor = (sampleBlockTex * sampleLightTex);
    diffuseColor *= v_Color;

    fragColor = _linearFog(diffuseColor, v_FragDistance, u_FogColor, u_FogStart, u_FogEnd);
}
					""");
		} else if (name.getPath().contains("blocks/block_layer_opaque.vsh")) {
			cir.setReturnValue("""
#version 150 core

#import <sodium:include/fog.glsl>
#import <sodium:include/chunk_vertex.glsl>
#import <sodium:include/chunk_parameters.glsl>
#import <sodium:include/chunk_matrices.glsl>

out vec4 v_Color;
out vec2 v_TexCoord;
out vec2 v_LightCoord;

#ifdef USE_FOG
out float v_FragDistance;
#endif

uniform int u_FogShape;
uniform vec3 u_RegionOffset;

uniform float tunnelStart;
uniform float tunnelEnd;

void main() {
    _vert_init();

    // Transform the chunk-local vertex position into world model space
    vec3 position = u_RegionOffset + _draw_translation + _vert_position;

#ifdef USE_FOG
    v_FragDistance = getFragDistance(u_FogShape, position);
#endif

	if (position.z < tunnelStart) {
		if (position.z > tunnelEnd) {
			float diff = position.z - tunnelStart;
			position.z = tunnelStart + (diff / 8);
		} else {
			position.z += 7;
		}
	}

    // Transform the vertex position into model-view-projection space
    gl_Position = u_ProjectionMatrix * u_ModelViewMatrix * vec4(position, 1.0);

    // Pass the color and texture coordinates to the fragment shader
    v_Color = _vert_color;
    v_LightCoord = _vert_tex_light_coord;
    v_TexCoord = _vert_tex_diffuse_coord;
}
				""");
		}
	}
}
