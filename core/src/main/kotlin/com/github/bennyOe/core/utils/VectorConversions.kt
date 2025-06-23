package com.github.bennyOe.core.utils

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.Viewport
import kotlin.math.cos
import kotlin.math.sin

fun degreesToLightDir(degrees: Float, elevation: Float = 1f): Vector3 {
    val rad = Math.toRadians(degrees.toDouble()).toFloat()
    return Vector3(cos(rad), sin(rad), elevation).nor()
}

fun worldToScreenSpace(
    world: Vector3,
    cam: OrthographicCamera,
    viewport: Viewport
): Vector3 {
    val tmp = cam.project(world.cpy())
    tmp.x = (tmp.x - viewport.screenX) / viewport.screenWidth
    tmp.y = (tmp.y - viewport.screenY) / viewport.screenHeight
    return tmp
}
