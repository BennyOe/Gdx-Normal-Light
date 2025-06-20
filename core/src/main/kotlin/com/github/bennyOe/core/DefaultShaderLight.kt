package com.github.bennyOe.core

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.github.bennyOe.core.utils.degreesToLightDir

class DefaultShaderLight(
    override var position: Vector2,
    override var color: Color,
    override var intensity: Float,
    override var direction: Vector3,
    override var falloff: Vector3,
    override var type: LightType,
    override var spotAngle: Float,
) : ShaderLight {

    override fun updateFrom(source: GameLight) {
        position.set(source.position)
        color.set(source.color)
        intensity = source.intensity
        direction.set(source.direction)
        falloff.set(source.falloff)
        type = source.type
        spotAngle = source.spotAngle
    }
}
