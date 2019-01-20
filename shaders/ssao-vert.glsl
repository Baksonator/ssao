#version 330

in vec3 in_Position;
in vec2 in_UV;

out vec2 var_UV;

void main()
{
    var_UV = in_UV;
    gl_Position = vec4(in_Position, 1.0);
}
