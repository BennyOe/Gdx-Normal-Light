package com.github.bennyOe.core

import box2dLight.ConeLight
import box2dLight.DirectionalLight
import box2dLight.Light
import box2dLight.PointLight
import com.badlogic.gdx.math.Vector2

interface IGameLight {
    fun update()
    fun remove()
}

sealed class GameLight(
    open val data: ShaderLight,
    open val b2dLight: Light
) : IGameLight {

    abstract override fun update()
    override fun remove() {}

    data class Directional(
        override val data: ShaderLight.Directional,
        override val b2dLight: DirectionalLight,
    ) : GameLight(data, b2dLight) {
        var intensity: Float
            get() = data.intensity
            set(value) {
                data.intensity = value
            }

        var direction: Float
            get() = data.direction
            set(value) {
                data.direction = value
            }

        override fun update() {
            val finalAlpha = data.color.a * data.intensity
            b2dLight.setColor(data.color.r, data.color.g, data.color.b, finalAlpha)
            b2dLight.direction = data.direction
        }
    }

    data class Point(
        override val data: ShaderLight.Point,
        override val b2dLight: PointLight
    ) : GameLight(data, b2dLight) {
        var intensity: Float
            get() = data.intensity
            set(value) {
                data.intensity = value
            }

        var position: Vector2
            get() = data.position
            set(value) {
                data.position = value
            }

        var distance: Float
            get() = data.distance
            set(value) {
                data.distance = value
            }

        override fun update() {
            val finalAlpha = data.color.a * data.intensity
            b2dLight.setColor(data.color.r, data.color.g, data.color.b, finalAlpha)
            b2dLight.distance = data.distance
            b2dLight.setPosition(data.position)
            println(b2dLight.distance)
        }
    }

    data class Spot(
        override val data: ShaderLight.Spot,
        override val b2dLight: ConeLight,
    ) : GameLight(data, b2dLight) {
        var intensity: Float
            get() = data.intensity
            set(value) {
                data.intensity = value
            }

        var position: Vector2
            get() = data.position
            set(value) {
                data.position = value
            }

        var direction: Float
            get() = data.direction
            set(value) {
                data.direction = value
            }

        var distance: Float
            get() = data.distance
            set(value) {
                data.distance = value
            }

        var coneDegree: Float
            get() = data.spotAngle
            set(value) {
                data.spotAngle = value
            }

        override fun update() {
            val finalAlpha = data.color.a * data.intensity
            b2dLight.setColor(data.color.r, data.color.g, data.color.b, finalAlpha)
            b2dLight.distance = data.distance
            b2dLight.position = data.position
            b2dLight.direction = data.direction
            b2dLight.coneDegree = data.spotAngle
        }
    }
}
