package com.github.bennyOe.core.utils

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.Viewport

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
