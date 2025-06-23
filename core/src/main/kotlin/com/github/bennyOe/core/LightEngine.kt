package com.github.bennyOe.core

import box2dLight.RayHandler
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.bennyOe.core.utils.degreesToLightDir
import com.github.bennyOe.core.utils.worldToScreenSpace
import ktx.math.vec3
import ktx.math.vec4
import java.lang.Math.toRadians
import kotlin.math.cos

class LightEngine(
    rayHandler: RayHandler,
    cam: OrthographicCamera,
    batch: SpriteBatch,
    viewport: Viewport,
    useDiffuseLight: Boolean = true,
    maxShaderLights: Int = 20,
) : AbstractLightEngine(rayHandler, cam, batch, viewport, useDiffuseLight, maxShaderLights) {

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
    }

    override fun applyShaderUniforms() {
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
            val data = lights[i].data
            val prefix = "[$i]"
            shader.setUniformf("lightColor$prefix", vec4(data.color.r, data.color.g, data.color.b, data.color.a * data.intensity))

            when (data) {
                is ShaderLight.Directional -> {
                    shader.setUniformi("lightType$prefix", 0)
                    val directionVector = degreesToLightDir(data.direction, data.elevation)
                    shader.setUniformf("lightDir$prefix", directionVector)
                }

                is ShaderLight.Point -> {
                    shader.setUniformi("lightType$prefix", 1)

                    val screenPos = worldToScreenSpace(vec3(data.position.x, data.position.y, 0f), cam, viewport)
                    shader.setUniformf("lightPos[$i]", screenPos)
                    shader.setUniformf("falloff$prefix", data.falloff)
                }

                is ShaderLight.Spot -> {
                    shader.setUniformi("lightType$prefix", 2)

                    val screenPos = worldToScreenSpace(vec3(data.position.x, data.position.y, 0f), cam, viewport)
                    shader.setUniformf("lightPos[$i]", screenPos)
                    shader.setUniformf("falloff$prefix", data.falloff)
                    val directionVector = degreesToLightDir(data.direction)
                    shader.setUniformf("lightDir$prefix", directionVector)
                    shader.setUniformf("coneAngle$prefix", cos(Math.toRadians(data.spotAngle.toDouble())).toFloat())
                }
            }
        }
    }
}
