package com.github.bennyOe

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.graphics.use
import ktx.math.vec3
import ktx.math.vec4
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import kotlin.math.acos
import kotlin.math.cos

class Light : KtxScreen {
    private lateinit var batch: SpriteBatch
    private lateinit var cam: OrthographicCamera
    private lateinit var wall: Texture
    private lateinit var wallNormals: Texture
    private lateinit var shader: ShaderProgram

    private var normalInfluenceValue = 1.0f
    private var coneAngleValue = cos(toRadians(20.0)).toFloat()
    private var lightType: LightType = LightType.DIRECTIONAL
    private var lightIntensity: Float = 1f
    private var r = 1f
    private var g = 1f
    private var b = 1f

    private val lightPos = vec3(0f, 0f, 0f)
    private var lightColor = vec4(r, g, b, lightIntensity)
    private val ambientColor = vec4(0.6f, 0.6f, 1f, 0.1f)
    private val falloff = vec3(.4f, 3f, 20f)
    private val vertShader: FileHandle = Gdx.files.internal("shader/light.vert")
    private val fragShader: FileHandle = Gdx.files.internal("shader/light.frag")


    override fun show() {
        wall = Texture("wall.png")
        wallNormals = Texture("wall_normal.png")

        ShaderProgram.pedantic = false
        shader = ShaderProgram(vertShader, fragShader)

        if (!shader.isCompiled) {
            throw GdxRuntimeException("Could not compile shader: ${shader.log}")
        }

        shader.bind()
        shader.setUniformi("u_normals", 1)
        shader.setUniformi("LightType", lightType.ordinal)
        shader.setUniformf("LightColor", lightColor)
        shader.setUniformf("AmbientColor", ambientColor)
        shader.setUniformf("Falloff", falloff)
        coneAngleValue = cos(toRadians(20.0)).toFloat()
        shader.setUniformf("ConeAngle", coneAngleValue)
        shader.setUniformf("normalInfluence", 1.0f)

        batch = SpriteBatch(1000, shader)
        batch.shader = shader

        cam = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        cam.setToOrtho(false)

        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun scrolled(amountX: Float, amountY: Float): Boolean {
                val angleDeg = toDegrees(acos(coneAngleValue.toDouble())) + amountY
                coneAngleValue = cos(toRadians(angleDeg.coerceIn(1.0, 89.0))).toFloat()
                return true
            }
        }
    }

    override fun resize(width: Int, height: Int) {
        cam.setToOrtho(false, width.toFloat(), height.toFloat())
        batch.projectionMatrix = cam.combined
        shader.bind()
        shader.setUniformf("Resolution", width.toFloat(), height.toFloat())
    }

    override fun render(delta: Float) {
        println(Gdx.graphics.framesPerSecond)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.use {
            val x = Gdx.input.x.toFloat() / Gdx.graphics.width
            val y = 1f - Gdx.input.y.toFloat() / Gdx.graphics.height

            lightPos.set(x, y, 0f)
            shader.setUniformf("LightPos", lightPos)

            val lightDir = vec3(-1f, -1f, 0f)
            shader.setUniformf("LightDir", lightDir)

            keyboardInput()

            wallNormals.bind(1)
            wall.bind(0)

            batch.draw(wall, 0f, 0f)
        }
    }

    private fun keyboardInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            normalInfluenceValue = (normalInfluenceValue - 0.01f).coerceAtLeast(0f)
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            normalInfluenceValue = (normalInfluenceValue + 0.01f).coerceAtMost(1f)
        }
        shader.setUniformf("normalInfluence", normalInfluenceValue)

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            lightIntensity += 0.05f
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            lightIntensity -= 0.05f
        }
        if (Gdx.input.isKeyPressed(Input.Keys.R)) {
            r = 1f
            g = 0f
            b = 0f
        } else if (Gdx.input.isKeyPressed(Input.Keys.G)) {
            r = 0f
            g = 1f
            b = 0f
        } else if (Gdx.input.isKeyPressed(Input.Keys.B)) {
            r = 0f
            g = 0f
            b = 1f
        } else if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            r = 1f
            g = 1f
            b = 1f
        }
        lightColor = vec4(r, g, b, lightIntensity)
        shader.setUniformf("LightColor", lightColor)

        if (Gdx.input.isKeyPressed((Input.Keys.NUM_1))) {
            lightType = LightType.DIRECTIONAL
        }
        if (Gdx.input.isKeyPressed((Input.Keys.NUM_2))) {
            lightType = LightType.SPOT
        }
        if (Gdx.input.isKeyPressed((Input.Keys.NUM_3))) {
            lightType = LightType.CONE
        }
        shader.setUniformi("LightType", lightType.ordinal)
    }

    override fun dispose() {
        batch.disposeSafely()
        wall.disposeSafely()
        wallNormals.disposeSafely()
        shader.disposeSafely()
    }
}

enum class LightType {
    DIRECTIONAL, SPOT, CONE
}
