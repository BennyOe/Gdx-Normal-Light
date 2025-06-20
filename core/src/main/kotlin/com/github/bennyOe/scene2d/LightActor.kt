package com.github.bennyOe.scene2d

import com.badlogic.gdx.scenes.scene2d.Actor
import com.github.bennyOe.core.GameLight

class LightActor(private val light: GameLight) : Actor() {
    override fun act(delta: Float) {
        super.act(delta)
        light.position.set(x, y)     // follow the actor
        light.update()               // sync to Box2D light
    }
}
