#ifdef GL_ES
    // Define LOWP for GL ES (mobile/web) environments for lower precision, can improve performance.
#define LOWP lowp
    // Set default precision for floating point numbers to medium. Required in GL ES fragment shaders.
precision mediump float;
#else
    // On desktop OpenGL, LOWP is not a keyword, so define it as empty.
#define LOWP
#endif

// Varying variables are passed from the vertex shader to the fragment shader.
// They are interpolated for each fragment.
varying LOWP vec4 v_color;      // The vertex color, usually white unless tinted.
varying vec2 v_texCoord;        // The texture coordinates (UVs) for the current fragment.

// Uniforms are variables that are constant for all vertices/fragments in a single draw call.
// They are set from the application (Kotlin code).
uniform sampler2D u_texture;    // The diffuse texture (the main color of the object).
uniform sampler2D u_normals;    // The normal map texture for this object.

uniform vec2 resolution;        // The resolution of the entire window/screen.
uniform float normalInfluence;  // How much the normal map affects the lighting (0.0 - 1.0).
uniform vec4 ambient;           // The ambient light color (rgb) and intensity (a).

#define MAX_LIGHTS 8            // The maximum number of lights the shader can process.

uniform int lightCount;         // The actual number of lights currently active.

// Viewport uniforms to handle screen areas correctly (e.g., with letterboxing).
uniform vec2 u_viewportOffset;  // The (x, y) offset of the viewport in screen pixels.
uniform vec2 u_viewportSize;    // The (width, height) of the viewport in screen pixels.

// Per-light properties, passed as arrays.
uniform int lightType[MAX_LIGHTS];      // 0:DIRECTIONAL, 1:POINT, 2:SPOT
uniform vec3 lightPos[MAX_LIGHTS];      // The light's position in normalized viewport space (0.0 - 1.0)
uniform vec3 lightDir[MAX_LIGHTS];      // The light's direction vector.
uniform vec4 lightColor[MAX_LIGHTS];    // The light's color (rgb) and intensity (a).
uniform float coneAngle[MAX_LIGHTS];    // For spotlights, this is the pre-calculated cosine of the cone's half-angle.
uniform vec3 falloff[MAX_LIGHTS];       // For point/spot lights, the constant, linear, and quadratic falloff factors.

void main() {
    // 1. Get base color from the diffuse texture.
    vec4 diffuseColor = texture2D(u_texture, v_texCoord);

    // 2. Get the normal vector from the normal map.
    // Texture values are in the 0-1 range, so they are mapped to the -1 to 1 range.
    vec3 normalMap = texture2D(u_normals, v_texCoord).rgb;
    vec3 n = normalize(normalMap * 2.0 - 1.0);

    // 3. Initialize the total light with the ambient light contribution.
    // This is the base light level that every fragment receives.
    vec3 totalLight = ambient.rgb * ambient.a;

    // 4. Calculate the current fragment's position in normalized viewport space (0.0 to 1.0).
    vec2 fragCoord_viewport = gl_FragCoord.xy - u_viewportOffset;
    vec3 fragPos = vec3(fragCoord_viewport / u_viewportSize, 0.0);

    // 5. Loop through all active dynamic lights and add their contributions.
    for (int i = 0; i < lightCount; i++) {
        vec3 l; // The final, normalized vector from the fragment to the light source.
        float attenuation = 1.0; // Default attenuation for directional light
        float spotFactor = 1.0; // Default factor is 1.0 (no effect).

        if (lightType[i] == 0) { // Light type 0 is DIRECTIONAL
                                 // For directional lights, the light vector is simply its direction.
                                 l = normalize(lightDir[i]);
        } else { // For POINT and SPOT lights
                 // Calculate the vector from the fragment to the light source.
                 vec3 fragToLight = lightPos[i] - fragPos;

                 // Correct the vector for the viewport's aspect ratio. This prevents stretching.
                 fragToLight.x *= u_viewportSize.x / u_viewportSize.y;

                 // The distance 'd' is needed for attenuation.
                 float d = length(fragToLight);
                 l = normalize(fragToLight);

                 // Calculate light attenuation based on distance (for point and spot lights).
                 attenuation = 1.0 / (falloff[i].x + (falloff[i].y * d) + (falloff[i].z * d * d));


                 if (lightType[i] == 2) { // Light type 2 is SPOT
                                          // The 'coneAngle' uniform already contains the cosine of the angle (the cutoff).
                                          float cutoff = coneAngle[i];

                                          // 'theta' is the cosine of the angle between the light's direction and the vector to the fragment.
                                          float theta = dot(normalize(lightDir[i]), -l);

                                          // Use smoothstep for a softer edge on the spotlight cone.
                                          spotFactor = smoothstep(cutoff - 0.05, cutoff, theta);
                 }
        }

        // Calculate the final diffuse contribution for this light.
        float baseDiffuse = max(dot(n, l), 0.0);
        float diffuseMix = mix(1.0, baseDiffuse, normalInfluence);
        vec3 diffuse = (lightColor[i].rgb * lightColor[i].a) * diffuseMix * attenuation * spotFactor;

        // Add this light's contribution to the total.
        totalLight += diffuse;
    }

    // 6. Calculate the final color by modulating the texture's color with the total accumulated light.
    vec3 finalColor = diffuseColor.rgb * totalLight;

    // 7. Set the final fragment color, retaining the original texture's alpha value.
    gl_FragColor = v_color * vec4(finalColor, diffuseColor.a);
}
