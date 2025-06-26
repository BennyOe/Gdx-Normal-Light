package com.github.bennyOe.demo

import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import com.github.bennyOe.core.GameLight
import com.github.bennyOe.core.LightEngine
import ktx.assets.disposeSafely
import ktx.math.vec2

class LightDemo : AbstractLightDemo() {
    private lateinit var lightEngine: LightEngine
    private lateinit var directionalLight: GameLight.Directional
    private lateinit var pointLight: GameLight.Point
    private lateinit var spotLight: GameLight.Spot

    override fun show() {
        super.show()
        lightEngine = LightEngine(rayHandler, cam, batch, viewport)

        directionalLight = lightEngine.addDirectionalLight(
            Color(0.8f, 0.8f, 1f, 0.45f),
            -45f,
            2.8f,
            40f
        )

//        pointLight = lightEngine.addPointLight(
//            vec2(6f, 6f),
//            Color(1f, 1f, 1f, 1f),
//            2f,
//            7f,
//            1f,
//            1f
//        )

        spotLight = lightEngine.addSpotLight(
            vec2(6f, 5f),
            Color(1f, 1f, 1f, 1f),
            0f,
            90f,
            4f,
            10f,
            0.5f,
            2f,
        )

        lightEngine.setNormalInfluence(0.8f)
    }

    override fun resize(width: Int, height: Int) {
        lightEngine.resize(width, height)
    }

    override fun render(delta: Float) {
        world.step(1 / 60f, 6, 2)

        cam.update()
        viewport.apply()

        controlDirectionalLight()
//        controlPointLight()
        controlSpotLight()

        followMousePos()
        lightEngine.update()

        batch.projectionMatrix = cam.combined

        lightEngine.renderLights {
            wallNormals.bind(1)
            wall.bind(0)
            batch.draw(wall, 0f, 0f, 19f, 9f)
        }
        debugRenderer.render(world, cam.combined)
    }

    private fun controlDirectionalLight() {
        if (Gdx.input.isKeyPressed(Input.Keys.I)) directionalLight.intensity += 0.1f
        if (Gdx.input.isKeyPressed(Input.Keys.K)) directionalLight.intensity -= 0.1f

        if (Gdx.input.isKeyPressed(Input.Keys.O)) directionalLight.direction += 0.1f
        if (Gdx.input.isKeyPressed(Input.Keys.L)) directionalLight.direction -= 0.1f
    }

    private fun controlPointLight() {
        if (Gdx.input.isKeyPressed(Input.Keys.T)) pointLight.shaderIntensity += 0.1f
        if (Gdx.input.isKeyPressed(Input.Keys.G)) pointLight.shaderIntensity -= 0.1f

        if (Gdx.input.isKeyPressed(Input.Keys.Y)) pointLight.distance += 0.1f
        if (Gdx.input.isKeyPressed(Input.Keys.H)) pointLight.distance -= 0.1f

        if (Gdx.input.isKeyPressed(Input.Keys.U)) pointLight.shaderBalance += 0.1f
        if (Gdx.input.isKeyPressed(Input.Keys.J)) pointLight.shaderBalance -= 0.1f

    }

    private fun controlSpotLight() {
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) spotLight.shaderIntensity += 0.1f
        if (Gdx.input.isKeyPressed(Input.Keys.A)) spotLight.shaderIntensity -= 0.1f

        if (Gdx.input.isKeyPressed(Input.Keys.W)) spotLight.distance += 0.1f
        if (Gdx.input.isKeyPressed(Input.Keys.S)) spotLight.distance -= 0.1f

        if (Gdx.input.isKeyPressed(Input.Keys.E)) spotLight.shaderBalance += 0.1f
        if (Gdx.input.isKeyPressed(Input.Keys.D)) spotLight.shaderBalance -= 0.1f

        if (Gdx.input.isKeyPressed(Input.Keys.R)) spotLight.coneDegree += 0.3f
        if (Gdx.input.isKeyPressed(Input.Keys.F)) spotLight.coneDegree -= 0.3f

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) RayHandler.useDiffuseLight(!RayHandler.isDiffuse)
    }

    private fun followMousePos() {
        val mousePos = Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
        viewport.unproject(mousePos)
        spotLight.position.set(mousePos.x, mousePos.y)
    }

    override fun dispose() {
        batch.disposeSafely()
        wall.disposeSafely()
        wallNormals.disposeSafely()
        lightEngine.dispose()
    }
}
