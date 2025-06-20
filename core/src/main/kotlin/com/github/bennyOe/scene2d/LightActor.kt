package com.github.bennyOe.scene2d

import com.badlogic.gdx.scenes.scene2d.Actor
import com.github.bennyOe.core.GameLight
import com.github.bennyOe.core.LightType
import com.github.bennyOe.core.utils.degreesToLightDir

class LightActor(private val light: GameLight) : Actor() {
    override fun act(delta: Float) {
        super.act(delta)
        light.position.set(x, y)     // follow the actor

        if (light.type == LightType.SPOT || light.type == LightType.DIRECTIONAL) {
            light.direction = degreesToLightDir(rotation)
        }

        light.update()
    }
}
