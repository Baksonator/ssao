#version 330

in vec2 var_UV;

uniform sampler2D uni_texture0;
uniform sampler2D uni_texture1;
uniform int flag;

out vec4 finalColor;

void main() {
    vec4 texSample = texture(uni_texture1, var_UV);
	float ambientOcclusion = texture(uni_texture0, var_UV).r;
    if (flag == 0)
    { 
    	finalColor = texSample * ambientOcclusion;
    } else 
    { 
    	finalColor = texSample;
    }
}
