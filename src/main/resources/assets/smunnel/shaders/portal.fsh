#version 150

uniform sampler2D Sampler0;
uniform ivec2 WindowSize;

out vec4 fragColor;

void main() {
    vec3 color = texture(Sampler0, gl_FragCoord.xy/WindowSize).rgb;
    fragColor = vec4(color, 1.0);
}
