package com.github.bennyOe.gdxNormalLight.scene2d

import com.badlogic.gdx.scenes.scene2d.Actor
import com.github.bennyOe.gdxNormalLight.core.GameLight
import ktx.math.vec2

class LightActor(
    private val light: GameLight,
) : Actor() {
    init {
        when (light) {
            is GameLight.Point -> setPosition(light.position.x, light.position.y)
            is GameLight.Spot -> setPosition(light.position.x, light.position.y)
            else -> Unit
        }
    }

    override fun act(delta: Float) {
        super.act(delta)

        when (light) {
            is GameLight.Point -> light.position = vec2(x, y)
            is GameLight.Spot -> light.position = vec2(x, y)
            else -> {}
        }

        light.update()
    }
}
