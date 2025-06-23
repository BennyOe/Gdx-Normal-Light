package com.github.bennyOe.core

import com.badlogic.gdx.math.Vector3
import ktx.math.vec3

data class Falloff(val constant: Float, val linear: Float, val quadratic: Float) {
    fun toVector3(): Vector3 = vec3(constant, linear, quadratic)

    companion object {
        fun fromDistance(distance: Float): Falloff {
            return when {
                distance <= 10f -> Falloff(1.0f, 0.7f, 1.8f)
                distance <= 30f -> Falloff(1.0f, 0.22f, 0.20f)
                distance <= 60f -> Falloff(1.0f, 0.09f, 0.032f)
                else -> Falloff(1.0f, 0.02f, 0.001f)
            }
        }
        val DEFAULT = Falloff(1.0f, 0.35f, 0.44f)
    }
}
