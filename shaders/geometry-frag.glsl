#version 330
layout (location = 0) out vec3 gPosition;
layout (location = 1) out vec3 gNormal;
layout (location = 2) out vec4 gColor;

in vec2 var_UV;
in vec3 var_Normal;
in vec3 var_Position;

void main()
{
    gPosition = var_Position;
    gNormal = normalize(var_Normal);
	gColor = vec4(1.0);
}
