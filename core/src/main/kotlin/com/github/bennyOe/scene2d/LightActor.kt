package com.github.bennyOe.scene2d

import com.badlogic.gdx.scenes.scene2d.Actor
import com.github.bennyOe.core.AbstractLightEngine
import com.github.bennyOe.core.GameLight

class LightActor(private val light: GameLight, private val engine: AbstractLightEngine) : Actor() {
    override fun act(delta: Float) {
        super.act(delta)
        light.b2dLight.position.set(x, y)     // follow the actor

        if (light is GameLight.Spot || light is GameLight.Directional) {
            light.b2dLight.direction = rotation
        }
        light.update()
    }
}
