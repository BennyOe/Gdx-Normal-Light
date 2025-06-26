package com.github.bennyOe.core

import box2dLight.RayHandler
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.bennyOe.core.utils.worldToScreenSpace
import ktx.math.vec3
import ktx.math.vec4
import kotlin.math.cos
import kotlin.math.sin

class LightEngine(
    rayHandler: RayHandler,
    cam: OrthographicCamera,
    batch: SpriteBatch,
    viewport: Viewport,
    useDiffuseLight: Boolean = false,
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
        shader.setUniformf("ambient", shaderAmbient)

        val screenX = viewport.screenX.toFloat()
        val screenY = viewport.screenY.toFloat()
        val screenW = viewport.screenWidth.toFloat()
        val screenH = viewport.screenHeight.toFloat()

        shader.setUniformf("u_viewportOffset", screenX, screenY)
        shader.setUniformf("u_viewportSize", screenW, screenH)

        for (i in shaderLights.indices) {
            val gameLight = lights[i]
            val data = lights[i].shaderLight
            val prefix = "[$i]"
            shader.setUniformf("lightColor$prefix", vec4(data.color.r, data.color.g, data.color.b, data.color.a * data.intensity))

            when (data) {
                is ShaderLight.Directional -> {
                    shader.setUniformi("lightType$prefix", 0)

                    val dirRad = Math.toRadians(data.direction.toDouble()).toFloat()
                    val eleRad = Math.toRadians(data.elevation.toDouble()).toFloat()

                    val directionVector = vec3(
                        cos(dirRad) * cos(eleRad),
                        sin(dirRad) * cos(eleRad),
                        sin(eleRad)
                    ).nor()

                    shader.setUniformf("lightDir$prefix", directionVector)
                }

                is ShaderLight.Point -> {
                    shader.setUniformi("lightType$prefix", 1)

                    val pointLight = gameLight as GameLight.Point
                    val shaderIntensity = data.intensity * pointLight.shaderBalance
                    shader.setUniformf("lightColor$prefix", vec4(data.color.r, data.color.g, data.color.b, data.color.a * shaderIntensity))

                    val screenPos = worldToScreenSpace(vec3(data.position.x, data.position.y, 0f), cam, viewport)
                    shader.setUniformf("lightPos[$i]", screenPos)
                    shader.setUniformf("falloff$prefix", data.falloff)
                }

                is ShaderLight.Spot -> {
                    shader.setUniformi("lightType$prefix", 2)

                    val pointLight = gameLight as GameLight.Spot
                    val shaderIntensity = data.intensity * pointLight.shaderBalance
                    shader.setUniformf("lightColor$prefix", vec4(data.color.r, data.color.g, data.color.b, data.color.a * shaderIntensity))

                    val screenPos = worldToScreenSpace(vec3(data.position.x, data.position.y, 0f), cam, viewport)
                    shader.setUniformf("lightPos[$i]", screenPos)
                    shader.setUniformf("falloff$prefix", data.falloff)

                    val rad = Math.toRadians(data.directionDegree.toDouble()).toFloat()
                    val directionVector = vec3(cos(rad), sin(rad), 0f)

                    shader.setUniformf("lightDir$prefix", directionVector)
                    shader.setUniformf("coneAngle$prefix", cos(Math.toRadians(data.coneDegree.toDouble() * 0.5)).toFloat())
                }
            }
        }
    }
}
