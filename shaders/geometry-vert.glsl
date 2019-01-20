#version 330

in vec3 in_Position;
in vec2 in_UV;
in vec3 in_Normal;

uniform mat4 uni_VP;
uniform mat4 uni_M;
uniform mat4 uni_MV;

out vec3 var_Normal;
out vec2 var_UV;
out vec3 var_Position;

void main()
{
    mat4 matMVP = uni_VP * uni_M;
    var_Position = (uni_MV * vec4(in_Position, 1.0)).xyz;
    gl_Position = matMVP * vec4(in_Position, 1.0);

    var_UV = in_UV;

    vec3 transformedNormal = (uni_MV * vec4(in_Normal, 0.0)).xyz;
    var_Normal = transformedNormal;
}
