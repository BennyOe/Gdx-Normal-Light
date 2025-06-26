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
    open val shaderLight: ShaderLight,
    open val b2dLight: Light
) : IGameLight {

    abstract override fun update()
    override fun remove() {}

    data class Directional(
        override val shaderLight: ShaderLight.Directional,
        override val b2dLight: DirectionalLight,
    ) : GameLight(shaderLight, b2dLight) {
        var intensity: Float
            get() = shaderLight.intensity
            set(value) {
                shaderLight.intensity = value
            }

        var color: Color
            get() = shaderLight.color
            set(value) {
                shaderLight.color = value
                b2dLight.color = value
            }

        var direction: Float
            get() = shaderLight.direction
            set(value) {
                shaderLight.direction = value
                b2dLight.direction = value
            }

        override fun update() {
            b2dLight.setColor(shaderLight.color.r, shaderLight.color.g, shaderLight.color.b, b2dLight.color.a)
        }
    }

    data class Point(
        override val shaderLight: ShaderLight.Point,
        override val b2dLight: PointLight,
        var shaderBalance: Float = 1.0f,
    ) : GameLight(shaderLight, b2dLight) {
        var position: Vector2
            get() = shaderLight.position
            set(value) {
                shaderLight.position = value
                b2dLight.position = value
            }

        var color: Color
            get() = shaderLight.color
            set(value) {
                shaderLight.color = value
                b2dLight.setColor(value)
            }

        // --- Independent Properties ---
        var shaderIntensity: Float
            get() = shaderLight.intensity
            set(value) {
                shaderLight.intensity = value
            }

        var distance: Float
            get() = b2dLight.distance
            set(value) {
                b2dLight.distance = value
                shaderLight.falloff = Falloff.fromDistance(value).toVector3()
            }

        override fun update() {
            b2dLight.setColor(shaderLight.color.r, shaderLight.color.g, shaderLight.color.b, b2dLight.color.a)
            b2dLight.position = shaderLight.position
        }
    }

    data class Spot(
        override val shaderLight: ShaderLight.Spot,
        override val b2dLight: ConeLight,
        var shaderBalance: Float = 1.0f,
    ) : GameLight(shaderLight, b2dLight) {
        var position: Vector2
            get() = shaderLight.position
            set(value) {
                shaderLight.position = value
                b2dLight.position = value
            }

        var direction: Float
            get() = shaderLight.directionDegree
            set(value) {
                shaderLight.directionDegree = value
                b2dLight.direction = value
            }

        var color: Color
            get() = shaderLight.color
            set(value) {
                shaderLight.color = value
                b2dLight.setColor(value.r, value.g, value.b, b2dLight.color.a)
            }


        // --- Independent Properties ---
        var shaderIntensity: Float
            get() = shaderLight.intensity
            set(value) {
                shaderLight.intensity = value
            }

        var distance: Float
            get() = b2dLight.distance
            set(value) {
                b2dLight.distance = value
                shaderLight.falloff = Falloff.fromDistance(value).toVector3()
            }

        var coneDegree: Float
            get() = shaderLight.coneDegree
            set(value) {
                shaderLight.coneDegree = value
                b2dLight.coneDegree = value / 2f
            }

        override fun update() {
            b2dLight.setColor(shaderLight.color.r, shaderLight.color.g, shaderLight.color.b, b2dLight.color.a)
            b2dLight.position = shaderLight.position
        }
    }
}
