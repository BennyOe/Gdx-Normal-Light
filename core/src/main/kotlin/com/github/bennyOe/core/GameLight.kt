package com.github.bennyOe.core

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

interface GameLight {
    var type: LightType
    var position: Vector2
    var color: Color
    var intensity: Float
    var direction: Vector2
    var falloff: Vector3
    var spotAngle: Float
    fun update()
}
