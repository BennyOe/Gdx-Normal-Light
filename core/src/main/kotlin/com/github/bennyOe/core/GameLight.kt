package com.github.bennyOe.core

import box2dLight.ConeLight
import box2dLight.DirectionalLight
import box2dLight.Light
import box2dLight.PointLight
import com.badlogic.gdx.graphics.Color
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

        var color: Color
            get() = data.color
            set(value) {
                data.color = value
                b2dLight.color = value
            }

        var direction: Float
            get() = data.direction
            set(value) {
                data.direction = value
                b2dLight.direction = value
            }

        override fun update() {
            b2dLight.setColor(data.color.r, data.color.g, data.color.b, b2dLight.color.a)
        }
    }

    data class Point(
        override val data: ShaderLight.Point,
        override val b2dLight: PointLight,
        var shaderBalance: Float = 1.0f,
    ) : GameLight(data, b2dLight) {
        var position: Vector2
            get() = data.position
            set(value) {
                data.position = value
                b2dLight.position = value
            }

        var color: Color
            get() = data.color
            set(value) {
                data.color = value
                b2dLight.setColor(value)
            }

        // --- Independent Properties ---
        var intensity: Float
            get() = data.intensity
            set(value) {
                data.intensity = value
            }

        var distance: Float
            get() = b2dLight.distance
            set(value) {
                b2dLight.distance = value
                data.falloff = Falloff.fromDistance(value).toVector3()
            }

        override fun update() {
            b2dLight.setColor(data.color.r, data.color.g, data.color.b, b2dLight.color.a)
        }
    }

    data class Spot(
        override val data: ShaderLight.Spot,
        override val b2dLight: ConeLight,
        var shaderBalance: Float = 1.0f,
    ) : GameLight(data, b2dLight) {
        var position: Vector2
            get() = data.position
            set(value) {
                data.position = value
                b2dLight.position = value
            }

        var direction: Float
            get() = data.directionDegree
            set(value) {
                data.directionDegree = value
                b2dLight.direction = value
            }

        var color: Color
            get() = data.color
            set(value) {
                data.color = value
                b2dLight.setColor(value.r, value.g, value.b, b2dLight.color.a)
            }


        // --- Independent Properties ---
        var shaderIntensity: Float
            get() = data.intensity
            set(value) {
                data.intensity = value
            }

        var distance: Float
            get() = b2dLight.distance
            set(value) {
                b2dLight.distance = value
                data.falloff = Falloff.fromDistance(value).toVector3()
            }

        var coneDegree: Float
            get() = data.coneDegree
            set(value) {
                data.coneDegree = value
                b2dLight.coneDegree = value / 2f
            }

        override fun update() {
            b2dLight.setColor(data.color.r, data.color.g, data.color.b, b2dLight.color.a)
        }
    }
}
