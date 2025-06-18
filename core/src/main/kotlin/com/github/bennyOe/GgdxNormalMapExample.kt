package com.github.bennyOe

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.async.KtxAsync
import ktx.graphics.use
import ktx.math.vec3
import ktx.math.vec4
import kotlin.math.max

class GgdxNormalMapExample : KtxGame<KtxScreen>() {
    override fun create() {
        KtxAsync.initiate()

        addScreen(FirstScreen())
        setScreen<FirstScreen>()
    }
}

const val DEFAULT_LIGHT_Z = 0.075f;
const val AMBIENT_INTENSITY = 0.2f;
const val LIGHT_INTENSITY = 1f;


class FirstScreen : KtxScreen {
    private lateinit var batch: SpriteBatch
    private lateinit var cam: OrthographicCamera
    private lateinit var wall: Texture
    private lateinit var wallNormals: Texture
    private lateinit var shader: ShaderProgram

    val LIGHT_POS = vec3(0f, 0f, DEFAULT_LIGHT_Z)
    val LIGHT_COLOR = vec4(1f, 0.8f, 0.6f, LIGHT_INTENSITY)
    val AMBIENT_COLOR = vec4(0.6f, 0.6f, 1f, AMBIENT_INTENSITY)
    val FALLOFF = vec3(.4f, 3f, 20f)

    val VERT = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
        "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" +
        "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +

        "uniform mat4 u_projTrans;\n" +
        " \n" +
        "varying vec4 vColor;\n" +
        "varying vec2 vTexCoord;\n" +

        "void main() {\n" +
        "	vColor = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" +
        "	vTexCoord = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +
        "	gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
        "}";

    val FRAG: String =  //GL ES specific stuff
        ("#ifdef GL_ES\n" //
            + "#define LOWP lowp\n" //
            + "precision mediump float;\n" //
            + "#else\n" //
            + "#define LOWP \n" //
            + "#endif\n" +  //
            "//attributes from vertex shader\n" +
            "varying LOWP vec4 vColor;\n" +
            "varying vec2 vTexCoord;\n" +
            "\n" +
            "//our texture samplers\n" +
            "uniform sampler2D u_texture;   //diffuse map\n" +
            "uniform sampler2D u_normals;   //normal map\n" +
            "\n" +
            "//values used for shading algorithm...\n" +
            "uniform vec2 Resolution;         //resolution of screen\n" +
            "uniform vec3 LightPos;           //light position, normalized\n" +
            "uniform LOWP vec4 LightColor;    //light RGBA -- alpha is intensity\n" +
            "uniform LOWP vec4 AmbientColor;  //ambient RGBA -- alpha is intensity \n" +
            "uniform vec3 Falloff;            //attenuation coefficients\n" +
            "\n" +
            "void main() {\n" +
            "	//RGBA of our diffuse color\n" +
            "	vec4 DiffuseColor = texture2D(u_texture, vTexCoord);\n" +
            "	\n" +
            "	//RGB of our normal map\n" +
            "	vec3 NormalMap = texture2D(u_normals, vTexCoord).rgb;\n" +
            "	\n" +
            "	//The delta position of light\n" +
            "	vec3 LightDir = vec3(LightPos.xy - (gl_FragCoord.xy / Resolution.xy), LightPos.z);\n" +
            "	\n" +
            "	//Correct for aspect ratio\n" +
            "	LightDir.x *= Resolution.x / Resolution.y;\n" +
            "	\n" +
            "	//Determine distance (used for attenuation) BEFORE we normalize our LightDir\n" +
            "	float D = length(LightDir);\n" +
            "	\n" +
            "	//normalize our vectors\n" +
            "	vec3 N = normalize(NormalMap * 2.0 - 1.0);\n" +
            "	vec3 L = normalize(LightDir);\n" +
            "	\n" +
            "	//Pre-multiply light color with intensity\n" +
            "	//Then perform \"N dot L\" to determine our diffuse term\n" +
            "	vec3 Diffuse = (LightColor.rgb * LightColor.a) * max(dot(N, L), 0.0);\n" +
            "\n" +
            "	//pre-multiply ambient color with intensity\n" +
            "	vec3 Ambient = AmbientColor.rgb * AmbientColor.a;\n" +
            "	\n" +
            "	//calculate attenuation\n" +
            "	float Attenuation = 1.0 / ( Falloff.x + (Falloff.y*D) + (Falloff.z*D*D) );\n" +
            "	\n" +
            "	//the calculation which brings it all together\n" +
            "	vec3 Intensity = Ambient + Diffuse * Attenuation;\n" +
            "	vec3 FinalColor = DiffuseColor.rgb * Intensity;\n" +
            "	gl_FragColor = vColor * vec4(FinalColor, DiffuseColor.a);\n" +
            "}")

    override fun show() {
        wall = Texture("wall.png")
        wallNormals = Texture("wall_normal.png")

        ShaderProgram.pedantic = false

        shader = ShaderProgram(VERT, FRAG)

        if (!shader.isCompiled) {
            throw GdxRuntimeException("Could not compile shader: ${shader.log}")
        }

        if (shader.log.isNotEmpty()) {
            println(shader.log)
        }

        shader.bind()

        shader.setUniformi("u_normals", 1)
        shader.setUniformf("LightColor", LIGHT_COLOR)
        shader.setUniformf("AmbientColor", AMBIENT_COLOR)
        shader.setUniformf("Falloff", FALLOFF)

        batch = SpriteBatch(1000, shader)
        batch.shader = shader

        cam = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        cam.setToOrtho(false)

        Gdx.input.inputProcessor = object : InputAdapter() {
            fun scrolled(delta: Int): Boolean {
                //LibGDX mouse wheel is inverted compared to lwjgl-basics
                LIGHT_POS.z = max(0f, LIGHT_POS.z - (delta * 0.005f))
                println("New light Z: " + LIGHT_POS.z)
                return true
            }
        }
    }

    override fun resize(width: Int, height: Int) {
        cam.setToOrtho(false, width.toFloat(), height.toFloat())
        batch.projectionMatrix = cam.combined
        shader.bind()
        shader.setUniformf("Resolution", width.toFloat(), height.toFloat());

    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        if (Gdx.input.isTouched) {
            LIGHT_POS.z = DEFAULT_LIGHT_Z
            println("New  light Z: ${LIGHT_POS.z}")
        }

        batch.use {
            val x = Gdx.input.x.toFloat() / Gdx.graphics.width
            val y = Gdx.input.y.toFloat() * -1 / Gdx.graphics.height

            LIGHT_POS.x = x
            LIGHT_POS.y = y

            shader.setUniformf("LightPos", LIGHT_POS)

            wallNormals.bind(1)

            wall.bind(0)

            batch.draw(wall, 0f, 0f)
        }
    }

    override fun dispose() {
        batch.disposeSafely()
    }
}
