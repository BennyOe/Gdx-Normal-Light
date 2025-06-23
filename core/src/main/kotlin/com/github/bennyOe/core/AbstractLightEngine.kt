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

abstract class AbstractLightEngine(
    val rayHandler: RayHandler,
    val cam: OrthographicCamera,
    val batch: SpriteBatch,
    val viewport: Viewport,
    val useDiffuseLight: Boolean = false,
    val maxShaderLights: Int = 20,
) {
    protected val vertShader: FileHandle = Gdx.files.internal("shader/light.vert")
    protected val fragShader: FileHandle = Gdx.files.internal("shader/light.frag")
    protected lateinit var shader: ShaderProgram
    protected lateinit var shaderAmbientLight: Color
    protected val lights = mutableListOf<GameLight>()
    protected val shaderLights get() = lights.take(maxShaderLights)
    protected var normalInfluenceValue: Float = 1f

    init {
        setupShader()
        setAmbientLight(Color(1f, 1f, 1f, 0.05f))
        RayHandler.useDiffuseLight(true)
        batch.shader = shader
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

    fun addDirectionalLight(
        color: Color,
        direction: Float,
        intensity: Float,
        elevation: Float = 1f,
        rays: Int = 128
    ): GameLight {
        val correctedDirection = -direction
        val shaderLight = ShaderLight.Directional(
            color = color,
            intensity = intensity,
            direction = correctedDirection,
            elevation = elevation,
        )
        val b2dLight = DirectionalLight(
            rayHandler,
            rays,
            color,
            correctedDirection,
        )

        val gameLight = GameLight.Directional(shaderLight, b2dLight)

        lights.add(gameLight)
        return gameLight
    }

    fun addPointLight(
        position: Vector2,
        color: Color,
        distance: Float = 1f,
        rays: Int = 128,
    ): GameLight {
        val falloff = Falloff.fromDistance(distance).toVector3()

        val shaderLight = ShaderLight.Point(
            color = color,
            intensity = color.a,
            position = position,
            falloff = falloff,
            distance = distance,
        )
        val b2dLight = PointLight(
            rayHandler,
            rays,
            color,
            distance,
            position.x,
            position.y
        )

        val gameLight = GameLight.Point(shaderLight, b2dLight)

        lights.add(gameLight)
        return gameLight
    }

    fun addSpotLight(
        position: Vector2,
        color: Color,
        direction: Float,
        coneDegree: Float,
        distance: Float = 1f,
        rays: Int = 128,
    ): GameLight {
        val falloff = Falloff.fromDistance(distance).toVector3()
        val correctedDirection = -direction

        val shaderLight = ShaderLight.Spot(
            color = color,
            intensity = color.a,
            position = position,
            falloff = falloff,
            direction = correctedDirection,
            spotAngle = coneDegree,
            distance = distance,
        )
        val b2dLight = ConeLight(
            rayHandler,
            rays,
            color,
            distance,
            position.x,
            position.y,
            correctedDirection,
            coneDegree,
        )

        val gameLight = GameLight.Spot(shaderLight, b2dLight)

        lights.add(gameLight)
        return gameLight

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

    fun dispose() {
        rayHandler.disposeSafely()
        shader.disposeSafely()
    }

    fun renderLights(drawScene: () -> Unit) {
        viewport.apply()
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.shader = shader
        batch.begin()

        applyShaderUniforms()
        drawScene()

        batch.end()
        batch.shader = null

        rayHandler.setCombinedMatrix(cam)
        rayHandler.updateAndRender()
    }

    abstract fun applyShaderUniforms()

    open fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        val scale = Gdx.graphics.backBufferScale
        rayHandler.setCombinedMatrix(cam)
        shader.bind()
        shader.setUniformf("resolution", width.toFloat() * scale, height.toFloat() * scale)
    }
}
