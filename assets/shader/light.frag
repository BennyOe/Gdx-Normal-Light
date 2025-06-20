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

uniform vec2 resolution;
uniform float normalInfluence;
uniform vec4 ambient;

#define MAX_LIGHTS 8

uniform int lightCount;

uniform vec2 u_viewportOffset; // = (screenX, screenY)
uniform vec2 u_viewportSize;   // = (screenWidth, screenHeight)

uniform int lightType[MAX_LIGHTS];
uniform vec3 lightPos[MAX_LIGHTS];
uniform vec3 lightDir[MAX_LIGHTS];
uniform vec4 lightColor[MAX_LIGHTS];
uniform float coneAngle[MAX_LIGHTS];
uniform vec3 falloff[MAX_LIGHTS];

void main() {
    vec4 diffuseColor = texture2D(u_texture, v_texCoord);
    vec3 normalMap = texture2D(u_normals, v_texCoord).rgb;
    vec3 n = normalize(normalMap * 2.0 - 1.0);
    vec3 ambientComponent = ambient.rgb * diffuseColor.rgb;
    vec3 totalLight = ambientComponent * ambient.a; // Initialize total light with ambient light
    vec2 screenCoord = gl_FragCoord.xy;
    vec2 vpCoord = screenCoord - u_viewportOffset;
    vec2 normCoord = vpCoord / u_viewportSize;
    vec3 fragPos = vec3(normCoord, 0.0);

    for (int i = 0; i < lightCount; i++) {
        vec3 l; // Light direction vector
        float d = 1.0; // Distance to light source
        float attenuation = 1.0;
        float spotFactor = 1.0;

        if (lightType[i] == 0) {
            // Directional light
            l = normalize(lightDir[i]); // Normalize the direction vector to ensure it has a length of 1
        } else {
            vec3 fragToLight = lightPos[i] - fragPos; // Calculate the vector from the fragment to the light source
            fragToLight.x *= resolution.x / resolution.y; // Adjust for aspect ratio
            d = length(fragToLight); // Calculate the distance to the light source
            l = normalize(fragToLight); // Normalize the vector to ensure it has a length of 1

            if (lightType[i] == 1) {
                // Point light
                attenuation = 1.0 / (falloff[i].x + (falloff[i].y * d) + (falloff[i].z * d * d));
            } else if (lightType[i] == 2) {
                // Spot light
                float theta = dot(normalize(lightDir[i]), -l); // Calculate the angle between the light direction and the vector to the fragment
                float epsilon = 0.2; // Small value to avoid division by zero
                spotFactor = smoothstep(coneAngle[i] - epsilon, coneAngle[i], theta); // Calculate the spot factor based on the angle
                attenuation = spotFactor / (falloff[i].x + (falloff[i].y * d) + (falloff[i].z * d * d)); // Calculate the attenuation based on the distance and falloff factors
            }
        }

        float baseDiffuse = max(dot(n, l), 0.0);
        float diffuseMix = mix(1.0, baseDiffuse, normalInfluence);
        vec3 diffuse = (lightColor[i].rgb * lightColor[i].a) * diffuseMix * attenuation * spotFactor;

        totalLight += diffuse;
    }

    vec3 finalColor = diffuseColor.rgb * totalLight;
    gl_FragColor = v_color * vec4(finalColor, diffuseColor.a);
}
