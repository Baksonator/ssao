#version 330

in vec3 in_Position;
in vec2 in_UV;
in vec3 in_Normal;

uniform mat4 uni_VP;
uniform mat4 uni_M;

uniform vec3 uni_lightColor1;
uniform vec3 uni_lightVector1;
uniform vec3 uni_ambient;
uniform vec3 uni_objectColor;

out vec2 var_UV;
out vec3 var_Position;
out vec3 var_Color;

void main()
{
    mat4 matMVP = uni_VP * uni_M;
    var_Position = (uni_M * vec4(in_Position, 1.0)).xyz;
    gl_Position = matMVP * vec4(in_Position, 1.0);

    var_UV = in_UV;

    vec3 transformedNormal = normalize((uni_M * vec4(in_Normal, 0.0)).xyz);
    
    float lightFactor = clamp(dot(transformedNormal, uni_lightVector1), 0, 1);
    var_Color = (uni_ambient * uni_objectColor) + (uni_objectColor * (uni_lightColor1 * lightFactor));
}
