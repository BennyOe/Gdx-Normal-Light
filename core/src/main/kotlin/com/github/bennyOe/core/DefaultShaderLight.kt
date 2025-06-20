package com.github.bennyOe.core

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

class DefaultShaderLight(
    override var position: Vector2,
    override var color: Color,
    override var direction: Vector2,
    override val intensity: Float,
    override var falloff: Vector3,
    override var type: LightType,
    override var spotAngle: Float,
) : ShaderLight {

    override fun updateFrom(source: GameLight) {
        position.set(source.position)
        color.set(source.color)
        direction.set(source.direction)
        falloff.set(source.falloff)
        type = source.type
        spotAngle = source.spotAngle
    }
}
