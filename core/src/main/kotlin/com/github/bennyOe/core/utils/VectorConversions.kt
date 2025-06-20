package com.github.bennyOe.core.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import ktx.math.vec3
import kotlin.math.cos
import kotlin.math.sin

fun degreesToLightDir(degrees: Float): Vector3 {
    val rad = Math.toRadians(degrees.toDouble()).toFloat()
    return Vector3(cos(rad), sin(rad), 1f).nor()
}

fun worldToScreenSpace(worldPos: Vector2, cam: Camera): Vector3 {
    val projected = cam.project(vec3(worldPos.x, worldPos.y, 0f))
    return Vector3(
        projected.x / Gdx.graphics.width,
        1f - projected.y / Gdx.graphics.height,
        0f
    )
}
