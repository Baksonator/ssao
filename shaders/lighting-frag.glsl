#version 330

uniform vec3 uni_lightColor1;
uniform vec3 uni_lightVector1;
uniform vec3 uni_ambient;
uniform vec3 uni_objectColor;

in vec2 var_UV;
in vec3 var_Position;
in vec3 var_Color;

out vec4 finalColor;

void main()
{
	finalColor = vec4(var_Color, 1.0);
}
