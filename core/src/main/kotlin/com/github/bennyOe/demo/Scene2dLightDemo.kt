package com.github.bennyOe.demo

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.github.bennyOe.core.Scene2dLightEngine
import com.github.bennyOe.scene2d.LightActor
import ktx.math.vec2


class Scene2dLightDemo : AbstractLightDemo() {
    private lateinit var stage: Stage
    private lateinit var lightEngine: Scene2dLightEngine
    private lateinit var actor: LightActor

    override fun show() {
        super.show()
        stage = Stage(viewport, batch)

        lightEngine = Scene2dLightEngine(rayHandler, cam, batch, viewport, stage)

        val light = lightEngine.addPointLight(
            vec2(16f, 6f),
            Color(1f, 0f, 1f, 1f),
            8f,
        )

        actor = LightActor(light, lightEngine)
        stage.addActor(actor)

        val label = Label("Scene2D working", Label.LabelStyle(BitmapFont(), Color.WHITE))
        label.setPosition(20f, 20f)
        stage.addActor(label)
    }


    override fun resize(width: Int, height: Int) {
        lightEngine.resize(width, height)
    }

    override fun render(delta: Float) {
        world.step(1 / 60f, 6, 2)

        cam.update()
        viewport.apply()

        val mouse = Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
        val worldPos = viewport.unproject(mouse)
        actor.setPosition(worldPos.x, worldPos.y)
        stage.act(delta)

        batch.projectionMatrix = cam.combined
        lightEngine.update()
        lightEngine.renderLights {
            wallNormals.bind(1)
            wall.bind(0)
            batch.draw(wall, 0f, 0f, 19f, 9f)
        }
        stage.draw()

        debugRenderer.render(world, cam.combined)
    }

    override fun dispose() {
        lightEngine.dispose()
        super.dispose()
    }

}
