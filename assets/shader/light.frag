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

uniform int LightType; // 0: Directional, 1: Point, 2: Spot
uniform vec2 Resolution;
uniform vec3 LightPos;
uniform vec3 LightDir;
uniform LOWP vec4 LightColor;
uniform LOWP vec4 AmbientColor;
uniform vec3 Falloff;
uniform float ConeAngle;
uniform float normalInfluence;

void main() {
    vec2 L;
    float D;
    float attenuation = 1.0;
    float spotFactor = 1.0;
    vec4 DiffuseColor = texture2D(u_texture, v_texCoord);
    vec3 NormalMap = texture2D(u_normals, v_texCoord).rgb;

    if (LightType == 0) {
        // Directional light
        L = normalize(LightDir.xy);
    }
    else {
        // Point or Spot light
        vec2 fragPos = gl_FragCoord.xy / Resolution.xy;
        vec2 fragToLight = LightPos.xy - fragPos;
        fragToLight.x *= Resolution.x / Resolution.y;

        D = length(fragToLight);
        L = normalize(fragToLight);

        if (LightType == 1) {
            // Point light
            attenuation = 1.0 / (Falloff.x + (Falloff.y * D) + (Falloff.z * D * D));
        } else if (LightType == 2) {
            // Spot light
            float theta = dot(normalize(LightDir.xy), -L);
            float epsilon = 0.2;
            spotFactor = smoothstep(ConeAngle - epsilon, ConeAngle, theta);
            attenuation = spotFactor / (Falloff.x + (Falloff.y * D) + (Falloff.z * D * D));
        }
    }

    vec3 N = normalize(NormalMap * 2.0 - 1.0);
    float baseDiffuse = max(dot(N.xy, L), 0.0);
    float diffuse = mix(1.0, baseDiffuse, normalInfluence);
    vec3 Diffuse = (LightColor.rgb * LightColor.a) * diffuse * attenuation * spotFactor;

    vec3 Ambient = AmbientColor.rgb * AmbientColor.a;
    // float Attenuation = 1.0 / (Falloff.x + (Falloff.y * D) + (Falloff.z * D * D));

    vec3 Intensity = Ambient + Diffuse;
    vec3 FinalColor = DiffuseColor.rgb * Intensity;

    gl_FragColor = v_color * vec4(FinalColor, DiffuseColor.a);
}
