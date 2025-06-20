package com.github.bennyOe.core

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

interface ShaderLight {
    val position: Vector2
    val color: Color
    val intensity: Float
    val direction: Vector3
    val falloff: Vector3
    val type: LightType
    val spotAngle: Float

    fun updateFrom(source: GameLight)
}
