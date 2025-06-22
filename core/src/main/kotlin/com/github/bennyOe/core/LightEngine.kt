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
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.bennyOe.core.utils.degreesToLightDir
import ktx.assets.disposeSafely
import ktx.math.vec3
import ktx.math.vec4
import java.lang.Math.toRadians
import kotlin.math.cos

class LightEngine(
    val rayHandler: RayHandler,
    val cam: OrthographicCamera,
    val batch: SpriteBatch,
    val viewport: Viewport,
    val maxShaderLights: Int = 20,
) {
    private val vertShader: FileHandle = Gdx.files.internal("shader/light.vert")
    private val fragShader: FileHandle = Gdx.files.internal("shader/light.frag")
    private lateinit var shader: ShaderProgram
    private lateinit var shaderAmbientLight: Color
    private val lights = mutableListOf<GameLight>()
    private val shaderLights get() = lights.take(maxShaderLights)
    private var normalInfluenceValue: Float = 1f

    init {
        setupShader()
        setAmbientLight(Color(1f, 1f, 1f, 0f))
        batch.shader = shader
    }

    fun addLight(
        type: LightType,
        position: Vector2,
        color: Color,
        direction: Float,
        intensity: Float = 1f,
        falloff: Vector3 = Vector3(1f, 0.1f, 0.01f),
        spotAngle: Float = 45f
    ): GameLight {

        val shaderLight = DefaultShaderLight(
            type = type,
            position = position,
            color = color,
            intensity = intensity,
            direction = degreesToLightDir(direction),
            falloff = falloff,
            spotAngle = spotAngle
        )

        val box2dLight = when (type) {
            LightType.POINT -> PointLight(rayHandler, 128, color, color.a * intensity, position.x, position.y)
            LightType.SPOT -> ConeLight(rayHandler, 128, color, color.a * intensity, position.x, position.y, direction, spotAngle)
            LightType.DIRECTIONAL -> DirectionalLight(rayHandler, 128, color, direction)
        }

        val combined = CombinedLight(
            type = type,
            position = position,
            color = color,
            intensity = intensity,
            directionAngle = direction,
            direction = degreesToLightDir(direction),
            falloff = falloff,
            spotAngle = spotAngle,
            shaderLight = shaderLight,
            box2dLight = box2dLight
        )

        lights.add(combined)
        return combined
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

    fun setAmbientLight(ambient: Color) {
        rayHandler.setAmbientLight(ambient)
        shaderAmbientLight = ambient
        shader.setUniformf("ambient", shaderAmbientLight)
    }

    fun setAmbientLight(r: Float, g: Float, b: Float, a: Float = 0.3f) {
        val ambient = Color(r, g, b, a)
        rayHandler.setAmbientLight(ambient)
        shaderAmbientLight = ambient
        shader.setUniformf("ambient", shaderAmbientLight)
    }

    fun update() = lights.forEach { it.update() }

    fun renderLights(drawScene: () -> Unit) {
        viewport.apply()
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.shader = shader
        batch.begin()

        applyShaderUniforms()
        drawScene()

        batch.end()
        batch.shader = null

//        rayHandler.setCombinedMatrix(cam)
//        rayHandler.updateAndRender()
    }

    fun resize(width: Int, height: Int) {
        val scale = Gdx.graphics.backBufferScale
        viewport.update(width, height, true)
        rayHandler.setCombinedMatrix(cam)
        shader.bind()
        shader.setUniformf("resolution", width.toFloat() * scale, height.toFloat() * scale)
    }

    fun dispose() {
        rayHandler.disposeSafely()
        shader.disposeSafely()
    }

    fun applyShaderUniforms() {
        val shader = batch.shader ?: return
        shader.bind()
        shader.setUniformi("lightCount", shaderLights.size)
        shader.setUniformf("normalInfluence", normalInfluenceValue)
        shader.setUniformf("ambient", shaderAmbientLight)

        val screenX = viewport.screenX.toFloat()
        val screenY = viewport.screenY.toFloat()
        val screenW = viewport.screenWidth.toFloat()
        val screenH = viewport.screenHeight.toFloat()

        shader.setUniformf("u_viewportOffset", screenX, screenY)
        shader.setUniformf("u_viewportSize", screenW, screenH)

        for (i in shaderLights.indices) {
            val light = shaderLights[i]
            val prefix = "[$i]"
            shader.setUniformi("lightType$prefix", light.type.ordinal)
//            val screen = cam.project(vec3(light.position, 0f))
//            val normX = (screen.x - viewport.screenX) / viewport.screenWidth
//            val normY = (screen.y - viewport.screenY) / viewport.screenHeight
            shader.setUniformf(
                "lightPos[$i]",
                vec3(
                    Gdx.input.x.toFloat() / Gdx.graphics.width.toFloat(),
                    1f - Gdx.input.y.toFloat() / Gdx.graphics.height.toFloat(),
                    0f
                )
            )
            shader.setUniformf("lightDir$prefix", light.direction)

            shader.setUniformf(
                "lightColor$prefix", vec4(
                    light.color.r,
                    light.color.g,
                    light.color.b,
                    light.color.a * light.intensity
                )
            )
            shader.setUniformf("falloff$prefix", light.falloff)
            shader.setUniformf("coneAngle$prefix", cos(toRadians(light.spotAngle.toDouble())).toFloat())
            shader.setUniformi("lightType$prefix", light.type.ordinal)
        }
    }

    private fun setupShader() {
        ShaderProgram.pedantic = false
        shader = ShaderProgram(vertShader, fragShader)

        if (!shader.isCompiled) {
            throw GdxRuntimeException("Could not compile shader: ${shader.log}")
        }

        shader.bind()
        shader.setUniformi("u_normals", 1)
    }
}
