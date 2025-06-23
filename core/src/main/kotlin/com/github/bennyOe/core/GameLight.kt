package com.github.bennyOe.core

import box2dLight.ConeLight
import box2dLight.DirectionalLight
import box2dLight.Light
import box2dLight.PointLight

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
    ) : GameLight(
        data,
        b2dLight,
    ) {
        override fun update() {
            b2dLight.color = data.color
            b2dLight.direction = data.direction
        }
    }

    data class Point(
        override val data: ShaderLight.Point,
        override val b2dLight: PointLight
    ) : GameLight(
        data,
        b2dLight
    ) {
        val finalAlpha = data.color.a * data.intensity
        override fun update() {
            b2dLight.setColor(data.color.r, data.color.g, data.color.b, finalAlpha)
            b2dLight.distance = data.distance
            b2dLight.setPosition(data.position)
        }
    }

    data class Spot(
        override val data: ShaderLight.Spot,
        override val b2dLight: ConeLight,
    ) : GameLight(
        data,
        b2dLight,
    ) {
        val finalAlpha = data.color.a * data.intensity
        override fun update() {
            b2dLight.setColor(data.color.r, data.color.g, data.color.b, finalAlpha)
            b2dLight.distance = data.distance
            b2dLight.position = data.position
            b2dLight.direction = data.direction
            b2dLight.coneDegree = data.spotAngle
        }
    }
}
