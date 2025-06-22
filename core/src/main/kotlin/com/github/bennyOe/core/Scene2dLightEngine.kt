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
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.bennyOe.core.utils.degreesToLightDir
import ktx.assets.disposeSafely
import ktx.log.logger
import ktx.math.vec2
import ktx.math.vec3
import ktx.math.vec4
import java.lang.Math.toRadians
import kotlin.math.cos

class Scene2dLightEngine(
    rayHandler: RayHandler,
    cam: OrthographicCamera,
    batch: SpriteBatch,
    viewport: Viewport,
    val stage: Stage?,
    maxShaderLights: Int = 20,
) : AbstractLightEngine(rayHandler, cam, batch, viewport, maxShaderLights) {

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
            val inputWorld = viewport.unproject(Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()))

            val normalizedX = (inputWorld.x - viewport.camera.position.x + viewport.worldWidth / 2f) / viewport.worldWidth
            val normalizedY = (inputWorld.y - viewport.camera.position.y + viewport.worldHeight / 2f) / viewport.worldHeight

            shader.setUniformf("lightPos[$i]", vec3(normalizedX, normalizedY, 0f))
            val label = Label("$normalizedX $normalizedY", Label.LabelStyle(BitmapFont(), Color.WHITE))
            label.setPosition(20f, 20f)
            stage.addActor(label)

//            shader.setUniformf(
//                "lightPos[$i]",
//                vec3(
//                    Gdx.input.x.toFloat() / Gdx.graphics.width.toFloat(),
//                    1f - Gdx.input.y.toFloat() / Gdx.graphics.height.toFloat(),
//                    0f
//                )
//            )
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
}
