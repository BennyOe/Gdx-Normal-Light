#ifdef GL_ES
        #define LOWP lowp
        precision mediump float;
#else
        #define LOWP
        #endif

varying LOWP vec4 v_color;
varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform sampler2D u_normals;

uniform vec2 Resolution;
uniform float normalInfluence;

#define MAX_LIGHTS 8

uniform int LightCount;

uniform int LightType[MAX_LIGHTS];
uniform vec3 LightPos[MAX_LIGHTS];
uniform vec3 LightDir[MAX_LIGHTS];
uniform vec4 LightColor[MAX_LIGHTS];
uniform vec4 AmbientColor[MAX_LIGHTS];
uniform float ConeAngle[MAX_LIGHTS];
uniform vec3 Falloff[MAX_LIGHTS];

void main() {
    vec4 DiffuseColor = texture2D(u_texture, v_texCoord);
    vec3 NormalMap = texture2D(u_normals, v_texCoord).rgb;
    vec3 N = normalize(NormalMap * 2.0 - 1.0);
    vec3 TotalLight = vec3(0.0);
    vec2 fragPos = gl_FragCoord.xy / Resolution.xy;

    for (int i = 0; i < LightCount; i++) {
        vec2 L; // Light direction vector
        float D = 1.0; // Distance to light source
        float attenuation = 1.0;
        float spotFactor = 1.0;

        if (LightType[i] == 0) {
            // Directional light
            L = normalize(LightDir[i].xy); // Normalize the direction vector to ensure it has a length of 1
        } else {
            vec2 fragToLight = LightPos[i].xy - fragPos; // Calculate the vector from the fragment to the light source
            fragToLight.x *= Resolution.x / Resolution.y; // Adjust for aspect ratio
            D = length(fragToLight); // Calculate the distance to the light source
            L = normalize(fragToLight); // Normalize the vector to ensure it has a length of 1

            if (LightType[i] == 1) {
                // Point light
                attenuation = 1.0 / (Falloff[i].x + (Falloff[i].y * D) + (Falloff[i].z * D * D));
            } else if (LightType[i] == 2) {
                // Spot light
                float theta = dot(normalize(LightDir[i].xy), -L); // Calculate the angle between the light direction and the vector to the fragment
                float epsilon = 0.2; // Small value to avoid division by zero
                spotFactor = smoothstep(ConeAngle[i] - epsilon, ConeAngle[i], theta); // Calculate the spot factor based on the angle
                attenuation = spotFactor / (Falloff[i].x + (Falloff[i].y * D) + (Falloff[i].z * D * D)); // Calculate the attenuation based on the distance and falloff factors
            }
        }

        float baseDiffuse = max(dot(N.xy, L), 0.0);
        float diffuse = mix(1.0, baseDiffuse, normalInfluence);
        vec3 Diffuse = (LightColor[i].rgb * LightColor[i].a) * diffuse * attenuation * spotFactor;
        vec3 Ambient = AmbientColor[i].rgb * AmbientColor[i].a; // Ambient light contribution

        TotalLight += Ambient + Diffuse;
    }

    vec3 FinalColor = DiffuseColor.rgb * TotalLight;
    gl_FragColor = v_color * vec4(FinalColor, DiffuseColor.a);
}
