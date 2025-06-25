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
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.assets.disposeSafely
import ktx.math.vec2
import ktx.math.vec3

abstract class AbstractLightEngine(
    val rayHandler: RayHandler,
    val cam: OrthographicCamera,
    val batch: SpriteBatch,
    val viewport: Viewport,
    val useDiffuseLight: Boolean,
    val maxShaderLights: Int = 20,
) {
    protected val vertShader: FileHandle = Gdx.files.internal("shader/light.vert")
    protected val fragShader: FileHandle = Gdx.files.internal("shader/light.frag")
    protected lateinit var shader: ShaderProgram
    protected lateinit var shaderAmbient: Color
    protected val lights = mutableListOf<GameLight>()
    protected val shaderLights get() = lights.take(maxShaderLights)
    protected var normalInfluenceValue: Float = 1f

    init {
        setupShader()
        RayHandler.useDiffuseLight(useDiffuseLight)
        setShaderAmbientLight(Color(1f, 1f, 1f, 0.2f))
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

    /**
     * Adds a new directional light to the scene. This light simulates a distant light source,
     * like the sun, where all light rays are parallel.
     *
     * The light is composed of a [ShaderLight] for visual effects on sprites and a
     * [box2dLight.DirectionalLight] for interactions within the Box2D world.
     *
     * @param color The color of the light. The alpha component is multiplied by the intensity.
     * @param direction The direction of the light in degrees, where 0 degrees points to the right (along the positive X-axis).
     * @param shaderIntensity The brightness of the light. This value is multiplied with the color's alpha component.
     * @param elevation The elevation of the light source in degrees. An elevation of 0 means the light is parallel to the XY plane.
     * An elevation of 90 degrees would mean the light shines straight down from the Z-axis. This is used to calculate the 3D light vector for the shader.
     * @param rays The number of rays used for the Box2D light. More rays produce higher quality shadows but are more performance-intensive.
     * @return The created [GameLight.Directional] instance, which can be used to modify the light's properties later.
     */
    fun addDirectionalLight(
        color: Color,
        direction: Float,
        shaderIntensity: Float,
        elevation: Float = 1f,
        rays: Int = 128
    ): GameLight.Directional {
        val correctedDirection = -direction
        val shaderLight = ShaderLight.Directional(
            color = color,
            intensity = shaderIntensity,
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

    /**
     * Adds a new point light to the scene. This light emanates from a single
     * point in all directions.
     *
     * The light is composed of a [ShaderLight.Point] for visual effects on sprites and a
     * [box2dLight.PointLight] for interactions within the Box2D world.
     *
     * @param position The world position of the light source.
     * @param color The color of the light. The alpha component is multiplied by the intensity.
     * @param intensity The base intensity of the light, affecting both the visual shader and the b2dLight.
     * @param distance The maximum range of the light. This defines the radius for shadow casting and the falloff calculation.
     * @param rays The number of rays used for the Box2D light. More rays produce higher quality shadows but are more performance-intensive.
     * @param falloffProfile A value between 0.0 and 1.0 that controls the shape of the light's falloff. 0.0 is more linear, 1.0 is strongly quadratic.
     * @param shaderBalance A multiplier to fine-tune the visual intensity of the shader light relative to the b2dLight's base intensity.
     * @return The created [GameLight.Point] instance, which can be used to modify the light's properties later.
     */
    fun addPointLight(
        position: Vector2,
        color: Color,
        shaderIntensity: Float = 1f,
        b2dDistance: Float = 1f,
        falloffProfile: Float = 0.5f,
        shaderBalance: Float = 0.5f,
        rays: Int = 128,
    ): GameLight.Point {
        val falloff = Falloff.fromDistance(b2dDistance, falloffProfile).toVector3()

        val shaderLight = ShaderLight.Point(
            color = color,
            intensity = shaderIntensity,
            position = position,
            falloff = falloff,
            distance = b2dDistance,
        )
        val b2dLight = PointLight(
            rayHandler,
            rays,
            color,
            b2dDistance,
            position.x,
            position.y
        )

        val gameLight = GameLight.Point(shaderLight, b2dLight, shaderBalance)

        lights.add(gameLight)
        return gameLight
    }

    /**
     * Adds a new spotlight to the scene, which emits light in a cone shape from a specific point.
     *
     * The light is composed of a [ShaderLight.Spot] for visual effects on sprites and a
     * [box2dLight.ConeLight] for interactions within the Box2D world.
     *
     * @param position The world position of the light source.
     * @param color The color of the light. The alpha component is multiplied by the intensity.
     * @param direction The direction the light is pointing in degrees (e.g., 0 is right, 90 is up).
     * @param coneDegree The **full** angle of the light cone in degrees. A value of 60 creates a 60-degree wide cone.
     * @param shaderIntensity The base intensity of the light, affecting both the visual shader and the b2dLight.
     * @param b2dDistance The maximum range of the light. This defines the radius for shadow casting and the falloff calculation.
     * @param falloffProfile A value between 0.0 and 1.0 that controls the shape of the light's falloff. 0.0 is more linear, 1.0 is strongly quadratic.
     * @param shaderBalance A multiplier to fine-tune the visual intensity of the shader light relative to the b2dLight's base intensity.
     * @param rays The number of rays used for the Box2D light. More rays produce higher quality shadows but are more performance-intensive.
     * @return The created [GameLight.Spot] instance, which can be used to modify the light's properties later.
     */
    fun addSpotLight(
        position: Vector2,
        color: Color,
        direction: Float,
        coneDegree: Float,
        shaderIntensity: Float = 1f,
        b2dDistance: Float = 1f,
        falloffProfile: Float = 0f,
        shaderBalance: Float = 0.5f,
        rays: Int = 128,
    ): GameLight.Spot {
        val falloff = Falloff.fromDistance(b2dDistance, falloffProfile).toVector3()

        val shaderLight = ShaderLight.Spot(
            color = color,
            intensity = shaderIntensity,
            position = position,
            falloff = falloff,
            directionDegree = direction,
            coneDegree = coneDegree,
            distance = b2dDistance,
        )
        val b2dLight = ConeLight(
            rayHandler,
            rays,
            color,
            b2dDistance,
            position.x,
            position.y,
            direction,
            coneDegree / 2,
        )

        val gameLight = GameLight.Spot(shaderLight, b2dLight, shaderBalance)

        lights.add(gameLight)
        return gameLight

    }

    /**
     * Sets how strongly the normal map influences the lighting effect.
     * @param normalInfluenceValue A value from 0.0 (no influence, flat lighting) to 1.0 (full influence).
     */
    fun setNormalInfluence(normalInfluenceValue: Float) {
        this.normalInfluenceValue = normalInfluenceValue
    }

    /**
     * Removes a specific light from the engine.
     * @param light The [GameLight] instance to remove.
     */
    fun removeLight(light: GameLight) {
        lights.remove(light)
        shader.bind()
        shader.setUniformi("lightCount", lights.size)
    }

    /**
     * Removes all dynamic lights from the engine.
     */
    fun clearLights() {
        lights.clear()
        shader.bind()
        shader.setUniformi("lightCount", 0)
    }

    /**
     * Sets the ambient light for the scene in the shader.
     * This is the base light color and intensity that affects all objects,
     * regardless of dynamic lights.
     * @param ambient The [Color] to use for ambient light. The color's alpha component acts as the intensity.
     */
    fun setShaderAmbientLight(ambient: Color) {
        shaderAmbient = ambient
    }

    /**
     * Updates the state of all lights. This method should be called once per frame.
     *
     * It iterates through all [GameLight] instances and synchronizes their properties (like color, position, or distance)
     * with the underlying Box2D light objects. This ensures that any changes made to the lights
     * are applied before they are rendered.
     */
    fun update() = lights.forEach { it.update() }

    /**
     * Renders the entire scene with dynamic lighting.
     *
     * This function orchestrates the main rendering loop. It prepares the custom lighting shader,
     * draws the game scene using a provided lambda, and then renders the Box2D lights and shadows on top.
     *
     * The process is as follows:
     * 1. The viewport is applied and the screen is cleared.
     * 2. The custom lighting shader is bound to the SpriteBatch.
     * 3. All light uniforms (colors, positions, directions, etc.) are set in the shader via `applyShaderUniforms`.
     * 4. The `drawScene` lambda is executed. This is where you should draw all your game sprites
     * that will be affected by the dynamic lights. Their normal maps must be bound to texture unit 1.
     * 5. The Box2D `rayHandler` is updated to render the physical lights and shadows over the scene.
     *
     * @param drawScene A lambda function containing the code to draw your game world (e.g., `batch.draw(...)`).
     */
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

    fun dispose() {
        rayHandler.disposeSafely()
        shader.disposeSafely()
    }

}
