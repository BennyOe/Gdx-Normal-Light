package com.github.bennyOe.core

import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.bennyOe.scene2d.NormalMappedActor

class Scene2dLightEngine(
    rayHandler: RayHandler,
    cam: OrthographicCamera,
    batch: SpriteBatch,
    viewport: Viewport,
    val stage: Stage?,
    useDiffuseLight: Boolean = true,
    maxShaderLights: Int = 20,
) : AbstractLightEngine(rayHandler, cam, batch, viewport, useDiffuseLight, maxShaderLights) {
    /**
     * Performs the complete lighting render pass using normal mapping and Box2D shadows.
     *
     * This function sets up the shader, uploads all light properties, invokes a user-provided lambda to render the
     * scene with lighting, and then renders Box2D-based shadows on top. It must be called once per frame.
     *
     * ### What this function does:
     * - Configures the batch with the shader and camera matrix.
     * - Applies lighting-related uniforms to the shader (light count, color, falloff, direction, etc.).
     * - Calls the [drawScene] lambda where you render all visible objects using your own draw logic.
     * - Renders Box2D shadows via [RayHandler].
     *
     * ### Requirements inside [drawScene]:
     * - **Normal map must be bound to texture unit 1** before calling `batch.draw(...)`.
     * - **Diffuse texture must be bound to texture unit 0** before calling `batch.draw(...)`.
     * - Use the batch normally for rendering your sprites — lighting will be automatically applied by the shader.
     *
     * @param drawScene Lambda in which your game scene should be rendered with lighting applied.
     */
    fun renderLights(drawScene: (Scene2dLightEngine) -> Unit) {
        batch.projectionMatrix = cam.combined
        viewport.apply()
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.shader = shader
        applyShaderUniforms()
        batch.begin()

        drawScene(this)

        batch.end()
        batch.shader = null

        rayHandler.setCombinedMatrix(cam)
        rayHandler.updateAndRender()
    }

    /**
     * Renders a normal-mapped actor by binding its diffuse and normal map textures
     * to the correct texture units and drawing the diffuse texture to the screen.
     *
     * This method ensures proper use of the lighting shader by binding the diffuse
     * texture to texture unit 0 and the normal map to texture unit 1. If the normal
     * map differs from the previously used one, the sprite batch is flushed to avoid
     * rendering artifacts caused by texture binding changes.
     *
     * This method must be called **within** the `renderLights` lambda block, so that
     * lighting shaders and uniforms are active.
     *
     * @param actor The [NormalMappedActor] to draw, which provides both a diffuse
     *              and a normal map texture, along with position and size.
     */
    fun draw(actor: Actor) {
        if (actor !is NormalMappedActor) return
        if (actor.normalMapTexture != lastNormalMap && lastNormalMap != null) {
            batch.flush()
        }

        actor.normalMapTexture.bind(1)
        actor.diffuseTexture.bind(0)
        batch.draw(actor.diffuseTexture, actor.x, actor.y, actor.width, actor.height)
        lastNormalMap = actor.normalMapTexture
    }

    override fun resize(width: Int, height: Int) {
        if (stage == null) return
        stage.viewport.update(width, height, true)
        super.resize(width, height)
    }
}
