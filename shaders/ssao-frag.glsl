#version 330

out float fragColor;

in vec2 var_UV;

uniform sampler2D uni_texture0;  // gPosition
uniform sampler2D uni_texture1;  // gNormal
uniform sampler2D uni_texture2;  // texNoise

uniform float samples[192];

uniform int kernelSize;
uniform float radius;
uniform float bias;

const vec2 noiseScale = vec2(1280.0/4.0, 720.0/4.0);

uniform mat4 uni_P;

void main()
{
    vec3 fragPos = texture(uni_texture0, var_UV).xyz;
    vec3 normal = texture(uni_texture1, var_UV).rgb;
    vec3 randomVec = texture(uni_texture2, var_UV * noiseScale).xyz;

    vec3 tangent = normalize(randomVec - normal * dot(randomVec, normal));
    vec3 bitangent = cross(normal, tangent);
    mat3 TBN = mat3(tangent, bitangent, normal);

    float occlusion = 0.0;

	for(int i = 0; i < kernelSize * 3; i += 3)
	{
		vec3 sample = vec3(samples[i], samples[i + 1], samples[i + 2]);
		sample = TBN * sample;
		sample = fragPos + sample * radius;

		vec4 offset = vec4(sample, 1.0);
		offset = uni_P * offset;
		offset.xyz /= offset.w;
		offset.xyz  = offset.xyz * 0.5 + 0.5;

		float sampleDepth = texture(uni_texture0, offset.xy).z;

		float rangeCheck = smoothstep(0.0, 1.0, radius / abs(fragPos.z - sampleDepth));

		occlusion += (sampleDepth >= sample.z + bias ? 1.0 : 0.0) * rangeCheck;
	}
    occlusion = 1.0 - (occlusion / kernelSize);
    
    fragColor = occlusion;
}
