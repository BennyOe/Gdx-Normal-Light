package com.github.bennyOe.core

import box2dLight.Light
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

class CombinedLight(
    override var type: LightType,
    override var position: Vector2,
    override var color: Color,
    override var intensity: Float,
    override var directionAngle: Float,
    override var direction: Vector3,
    override var falloff: Vector3,
    override var spotAngle: Float,
    private val shaderLight: ShaderLight,
    private val box2dLight: Light
) : GameLight {

    override fun update() {
        shaderLight.updateFrom(this)
        updateBox2dLight()
    }

    private fun updateBox2dLight() {
        box2dLight.color = color
        box2dLight.distance = color.a * intensity

        when (box2dLight) {
            is box2dLight.PointLight -> {
                box2dLight.setPosition(position)
            }

            is box2dLight.ConeLight -> {
                box2dLight.setPosition(position)
                box2dLight.setDirection(directionAngle)
                box2dLight.setConeDegree(spotAngle)
            }

            is box2dLight.DirectionalLight -> {
                box2dLight.setDirection(directionAngle)
            }
            // ggf. mehr Typen
        }
    }
}
