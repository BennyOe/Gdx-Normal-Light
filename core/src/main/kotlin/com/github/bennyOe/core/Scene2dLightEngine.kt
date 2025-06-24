package com.github.bennyOe.core

import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.math.vec3
import ktx.math.vec4

class Scene2dLightEngine(
    rayHandler: RayHandler,
    cam: OrthographicCamera,
    batch: SpriteBatch,
    viewport: Viewport,
    val stage: Stage?,
    useDiffuseLight: Boolean = true,
    maxShaderLights: Int = 20,
) : AbstractLightEngine(rayHandler, cam, batch, viewport, useDiffuseLight, maxShaderLights) {

    override fun resize(width: Int, height: Int) {
        if (stage == null) return
        stage.viewport.update(width, height, true)
        super.resize(width, height)
    }

    override fun applyShaderUniforms() {
        if (stage == null) return
        val shader = batch.shader ?: return
        shader.bind()
        shader.setUniformi("lightCount", shaderLights.size)
        shader.setUniformf("normalInfluence", normalInfluenceValue)
        shader.setUniformf("ambient", shaderAmbient)

        val screenX = viewport.screenX.toFloat()
        val screenY = viewport.screenY.toFloat()
        val screenW = viewport.screenWidth.toFloat()
        val screenH = viewport.screenHeight.toFloat()

        shader.setUniformf("u_viewportOffset", screenX, screenY)
        shader.setUniformf("u_viewportSize", screenW, screenH)

        for (i in shaderLights.indices) {
            val data = (lights[i] as GameLight).data
            val prefix = "[$i]"
            shader.setUniformf("lightColor$prefix", vec4(data.color.r, data.color.g, data.color.b, data.color.a * data.intensity))

            val inputWorld = viewport.unproject(Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()))

            val normalizedX = (inputWorld.x - viewport.camera.position.x + viewport.worldWidth / 2f) / viewport.worldWidth
            val normalizedY = (inputWorld.y - viewport.camera.position.y + viewport.worldHeight / 2f) / viewport.worldHeight

            shader.setUniformf("lightPos[$i]", vec3(normalizedX, normalizedY, 0f))

//            shader.setUniformf("lightDir$prefix", light.direction)
//
//            shader.setUniformf("falloff$prefix", light.falloff)
//            shader.setUniformf("coneAngle$prefix", cos(toRadians(light.spotAngle.toDouble())).toFloat())
//            shader.setUniformi("lightType$prefix", light.type.ordinal)
        }
    }
}
