package com.github.bennyOe.core

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

interface ShaderLight {
    val position: Vector2
    val color: Color
    val direction: Vector2
    val falloff: Vector3
    val intensity: Float
    val type: LightType
    val spotAngle: Float

    fun updateFrom(source: GameLight)
}
