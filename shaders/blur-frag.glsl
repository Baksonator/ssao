#version 330

out float fragColor;

in vec2 var_UV;

uniform sampler2D uni_texture0;

void main() {
    vec2 texelSize = 1.0 / vec2(textureSize(uni_texture0, 0));
    float result = 0.0;
    for (int x = -2; x < 2; ++x)
    {
        for (int y = -2; y < 2; ++y)
        {
            vec2 offset = vec2(float(x), float(y)) * texelSize;
            result += texture(uni_texture0, var_UV + offset).r;
        }
    }
    fragColor = result / (4.0 * 4.0);
}
