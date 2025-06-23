package com.github.bennyOe.demo

import com.badlogic.gdx.graphics.Color
import com.github.bennyOe.core.GameLight
import com.github.bennyOe.core.LightEngine
import com.github.bennyOe.core.ShaderLight
import ktx.assets.disposeSafely
import ktx.math.vec2

class LightDemo : AbstractLightDemo() {
    private lateinit var lightEngine: LightEngine
    private lateinit var light: GameLight.Point

    override fun show() {
        super.show()

        lightEngine = LightEngine(rayHandler, cam, batch, viewport)
        light = lightEngine.addPointLight(
            vec2(16f, 6f),
            Color(1f, 0f, 1f, 1f),
            12f,
        )
//        lightEngine.addDirectionalLight(Color(1f, 0.3f, 0.5f, 0.5f), 0f, 1f, 0.3f)

//        light = lightEngine.addSpotLight(
//            vec2(6f, 4f),
//            Color(1f, 0f, 1f, 1f),
//            0f,
//            60f,
//            12f,
//        )
    }

    override fun resize(width: Int, height: Int) {
        lightEngine.resize(width, height)
    }

    override fun render(delta: Float) {
        world.step(1 / 60f, 6, 2)

        cam.update()
        viewport.apply()

//        light.distance += 0.1f
        lightEngine.update()

        batch.projectionMatrix = cam.combined

        lightEngine.renderLights {
            wallNormals.bind(1)
            wall.bind(0)
            batch.draw(wall, 0f, 0f, 19f, 9f)
        }
        debugRenderer.render(world, cam.combined)
    }

    override fun dispose() {
        batch.disposeSafely()
        wall.disposeSafely()
        wallNormals.disposeSafely()
        lightEngine.dispose()
    }
}
