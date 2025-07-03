package com.github.bennyOe.gdxNormalLight.core

import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.bennyOe.gdxNormalLight.scene2d.NormalMappedActor

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

        setShaderToEngineShader()
        applyShaderUniforms()

        batch.begin()
        drawScene(this)
        batch.end()

        setShaderToDefaultShader()

        rayHandler.setCombinedMatrix(cam)
        rayHandler.updateAndRender()
    }

    /**
     * Draws a sprite using both a diffuse and a normal map texture, applying normal mapping lighting effects.
     *
     * This method binds the normal map to texture unit 1 and the diffuse texture to unit 0, sets the appropriate
     * shader uniforms, and draws the sprite at the specified position and size. If `flipX` is true, the sprite
     * is drawn mirrored horizontally.
     *
     * Use this method within the [renderLights] lambda to ensure lighting and shader context are active.
     *
     * @param diffuse The diffuse [TextureRegion] (base color texture).
     * @param normals The normal map [TextureRegion] (for lighting effects).
     * @param x The x-coordinate to draw the sprite.
     * @param y The y-coordinate to draw the sprite.
     * @param width The width of the sprite.
     * @param height The height of the sprite.
     * @param flipX If true, the sprite is drawn mirrored on the X axis.
     */
    fun draw(
        diffuse: TextureRegion,
        normals: TextureRegion,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        flipX: Boolean = false,
    ) {
        shader.setUniformi("u_useNormalMap", 1)
        shader.setUniformi("u_useSpecularMap", 0)

        normals.texture.bind(1)
        diffuse.texture.bind(0)

        if (flipX) {
            batch.draw(diffuse, x + width, y, -width, height)
        } else {
            batch.draw(diffuse, x, y, width, height)
        }
    }

    /**
     * Draws a Scene2D [Image] without applying normal mapping.
     *
     * This method disables normal mapping in the shader, flushes the batch if a normal map was previously used,
     * and renders the given [Image] using the lighting shader with normal mapping turned off.
     *
     * Use this when rendering images that do not have an associated normal map, ensuring correct lighting behavior.
     *
     * @param image The [Image] to render without normal mapping.
     */
    fun drawWithoutNormalMap(image: Image) {
        // If we were just drawing with a normal map, flush the batch before changing the shader state.
        if (lastNormalMap != null) {
            batch.flush()
        }

        // Set our lighting shader and tell it to ignore the normal map uniform.
        batch.shader = this.shader
        shader.bind()
        shader.setUniformi("u_useNormalMap", 0)

        // Draw the image normally
        image.draw(batch, 1f)

        // Update the state to reflect that we are no longer using a normal map.
        lastNormalMap = null
    }

    /**
     * Draws a Scene2D [Actor] with optional normal mapping.
     *
     * If the actor is a [NormalMappedActor], it binds the normal map to texture unit 1
     * and the diffuse texture to unit 0, and renders it with lighting effects applied.
     * If the actor is not normal-mapped, it disables normal mapping in the shader and
     * renders the actor using the standard [Actor.draw] method.
     *
     * This method must be called **within** the [renderLights] lambda to ensure lighting
     * uniforms and shader context are active.
     *
     * @param actor The [Actor] to render. May or may not have an associated normal map.
     */
    fun draw(actor: Actor) {
        if (actor is NormalMappedActor) {
            if (lastNormalMap == null || lastNormalMap != actor.normalMapTexture) {
                batch.flush()
            }

            actor.normalMapTexture?.let {
                it.bind(1)
                shader.bind()
                shader.setUniformi("u_useNormalMap", 1)
                lastNormalMap = actor.normalMapTexture
            }
            actor.specularTexture?.let {
                shader.bind()
                shader.setUniformi("u_useSpecularMap", 1)
                it.bind(2)
                lastSpecularMap = actor.specularTexture
            }
            actor.diffuseTexture.bind(0)
            batch.draw(actor.diffuseTexture, actor.x, actor.y, actor.width, actor.height)
        } else {
            setShaderToDefaultShader()
            actor.draw(batch, 1.0f)
            lastNormalMap = null
            lastSpecularMap = null
        }
    }

    override fun resize(width: Int, height: Int) {
        if (stage == null) return
        stage.viewport.update(width, height, true)
        val screenX = stage.viewport.screenX * Gdx.graphics.backBufferScale.toInt()
        val screenY = stage.viewport.screenY * Gdx.graphics.backBufferScale.toInt()
        val screenW = stage.viewport.screenWidth * Gdx.graphics.backBufferScale.toInt()
        val screenH = stage.viewport.screenHeight * Gdx.graphics.backBufferScale.toInt()
        rayHandler.useCustomViewport(screenX, screenY, screenW, screenH)
        super.resize(width, height)
    }
}
