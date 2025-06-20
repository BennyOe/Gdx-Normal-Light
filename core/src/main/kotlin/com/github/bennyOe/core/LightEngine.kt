package com.github.bennyOe.core

import box2dLight.ConeLight
import box2dLight.DirectionalLight
import box2dLight.PointLight
import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.assets.disposeSafely
import ktx.graphics.use
import java.lang.Math.toRadians
import kotlin.math.cos

class LightEngine(
    val rayHandler: RayHandler,
    val world: World,
    val cam: OrthographicCamera,
    val batch: SpriteBatch,
    val maxShaderLights: Int = 20,
) {
    private val vertShader: FileHandle = Gdx.files.internal("shader/light.vert")
    private val fragShader: FileHandle = Gdx.files.internal("shader/light.frag")
    private lateinit var shader: ShaderProgram
    private var shaderAmbientLight: Color = Color(1f, 1f, 1f, 0.5f)
    private val lights = mutableListOf<GameLight>()
    private val shaderLights get() = lights.take(maxShaderLights)
    private var normalInfluenceValue: Float = 1f

    init {
        setupShader()
        batch.shader = shader
    }

    fun addLight(
        type: LightType,
        position: Vector2,
        color: Color,
        intensity: Float,
        direction: Vector2,
        falloff: Vector3 = Vector3(1f, 0.1f, 0.01f),
        spotAngle: Float = 45f
    ) {

        val shaderLight = DefaultShaderLight(
            type = type,
            position = position,
            color = color,
            intensity = intensity,
            direction = direction,
            falloff = falloff,
            spotAngle = spotAngle,
        )

        val box2dLight = when (type) {
            LightType.POINT -> PointLight(rayHandler, 128, color, intensity, position.x, position.y)
            LightType.SPOT -> ConeLight(rayHandler, 128, color, intensity, position.x, position.y, direction.angleDeg(), spotAngle)
            LightType.DIRECTIONAL -> DirectionalLight(rayHandler, 128, color, direction.x)
        }

        val combined = CombinedLight(
            type = type,
            position = position,
            color = color,
            intensity = intensity,
            direction = direction,
            falloff = falloff,
            spotAngle = spotAngle,
            shaderLight = shaderLight,
            box2dLight = box2dLight
        )

        lights.add(combined)
    }

    fun setNormalInfluence(normalInfluenceValue: Float) {
        shader.setUniformf("normalInfluence", normalInfluenceValue)
    }

    fun removeLight(light: GameLight) {
        lights.remove(light)
        shader.bind()
        shader.setUniformi("lightCount", lights.size)
    }

    fun clearLights() {
        lights.clear()
        shader.bind()
        shader.setUniformi("lightCount", 0)
    }

    fun setAmbientLight(color: Color, intensity: Float = 1f) {
        val ambient = color.cpy().mul(intensity)
        rayHandler.setAmbientLight(ambient)
        shaderAmbientLight = ambient
    }

    fun setAmbientLight(r: Float, g: Float, b: Float, a: Float = 1f) {
        val ambient = Color(r, g, b, a)
        rayHandler.setAmbientLight(ambient)
        shaderAmbientLight = ambient
    }

    fun update(deltaTime: Float) = lights.forEach { it.update() }

    fun renderLights(drawScene: () -> Unit) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        applyShaderUniforms()
        batch.use {
            drawScene()
        }
//        rayHandler.setCombinedMatrix(cam)
//        rayHandler.updateAndRender()
    }

    fun resize(width: Int, height: Int) {
        rayHandler.setCombinedMatrix(cam)
        shader.bind()
        shader.setUniformf("resolution", width.toFloat(), height.toFloat())
    }

    fun dispose() {
        rayHandler.disposeSafely()
        shader.disposeSafely()
    }

    fun applyShaderUniforms() {
        val shader = batch.shader ?: return
        for (i in shaderLights.indices) {
            val light = shaderLights[i]
            val prefix = "[$i]"
            shader.setUniformi("lightType$prefix", light.type.ordinal)
            shader.setUniformf("lightPos$prefix", light.position)
            shader.setUniformf("lightDir$prefix", light.direction)
            shader.setUniformf("lightColor$prefix", light.color)
            shader.setUniformf("falloff$prefix", light.falloff)
            shader.setUniformf("coneAngle$prefix", cos(toRadians(light.spotAngle.toDouble())).toFloat())
            shader.setUniformi("lightType$prefix", light.type.ordinal)
            shader.setUniformf("normalInfluence", normalInfluenceValue)
            shader.setUniformf("ambient", shaderAmbientLight)
        }
    }

    private fun setupShader() {
        ShaderProgram.pedantic = false
        shader = ShaderProgram(vertShader, fragShader)

        if (!shader.isCompiled) {
            throw GdxRuntimeException("Could not compile shader: ${shader.log}")
        }

        shader.bind()
        shader.setUniformi("lightCount", maxShaderLights)
        shader.setUniformi("u_normals", 1)
        shader.setUniformf("normalInfluence", normalInfluenceValue)
        shader.setUniformf("ambient", shaderAmbientLight)
    }
}
