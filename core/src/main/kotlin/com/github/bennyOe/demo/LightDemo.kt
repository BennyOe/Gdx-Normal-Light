package com.github.bennyOe.demo

import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.github.bennyOe.core.LightEngine
import com.github.bennyOe.core.LightType
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.math.vec2
import ktx.math.vec3

const val PPM = 100f

class LightDemo : KtxScreen {
    private val world = World(vec2(0f, -9.81f), true)
    private lateinit var cam: OrthographicCamera
    private lateinit var viewport: ExtendViewport
    private lateinit var lightEngine: LightEngine
    private lateinit var batch: SpriteBatch


    private lateinit var wall: Texture
    private lateinit var wallNormals: Texture

    private val rayHandler = RayHandler(world)
    private lateinit var wallBody: Body
    private val debugRenderer = Box2DDebugRenderer()


    override fun show() {
        cam = OrthographicCamera(19f, 9f)
        rayHandler.setBlurNum(3)
        RayHandler.useDiffuseLight(false)
        batch = SpriteBatch()
        viewport = ExtendViewport(19f, 9f, cam)
        wall = Texture("wall.png")
        wallNormals = Texture("wall_normal.png")

        lightEngine = LightEngine(rayHandler, cam, batch, viewport)
        createLights()
        createWalls()
    }

    override fun resize(width: Int, height: Int) {
        lightEngine.resize(width, height)
    }

    override fun render(delta: Float) {
        world.step(1 / 60f, 6, 2)
        println(Gdx.graphics.framesPerSecond)

        lightEngine.update()

        lightEngine.renderLights {
            wallNormals.bind(1)
            wall.bind(0)
            batch.draw(wall, 0f, 0f)
        }
        debugRenderer.render(world, cam.combined)
    }

    override fun dispose() {
        batch.disposeSafely()
        wall.disposeSafely()
        wallNormals.disposeSafely()
        lightEngine.dispose()
    }

    private fun createLights() {
//        lightEngine.setAmbientLight(Color(1f, 0f,0f, 0.2f))
//        lightEngine.addLight(LightType.DIRECTIONAL, vec2(1f, 1f), Color(1f, 1f, 1f, 0.5f), 70f)
//        lightEngine.addLight(
//            LightType.POINT,
//            vec2(-1f, -3f),
//            Color(1f, 0f, 1f, 1f),
//            70f,
//            3f,
//            vec3(.4f, 3f, 20f)
//        )
        lightEngine.addLight(
            LightType.SPOT,
            vec2(4f, 4f),
            Color(1f, 0f, 1f, 1f),
            20f,
            9f,
            vec3(.4f, 3f, 20f)
        )
    }

    private fun createWalls() {
        val wallPositions = listOf(
            vec2(-4f, -2.5f) to 0.2f,
            vec2(-3.0f, 2.0f) to 0.15f,
            vec2(-1.5f, 1.5f) to 0.18f,
            vec2(0.0f, -1.5f) to 0.13f,
            vec2(1.5f, 2.0f) to 0.17f,
            vec2(3.0f, -2.0f) to 0.14f,
            vec2(2.5f, 0.5f) to 0.12f,
            vec2(-2.0f, 0.0f) to 0.16f
        )
        for ((pos, size) in wallPositions) {
            createWallBody(world, pos, size)
        }
    }

    private fun createWallBody(world: World, position: Vector2, size: Float) {
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.StaticBody
            position.set(position) // sichtbar im Zentrum
        }

        wallBody = world.createBody(bodyDef)

        val shape = PolygonShape().apply {
            setAsBox(size, size, position, 0f) // große Fläche = sichtbar, zentriert
        }

        val fixtureDef = FixtureDef().apply {
            this.shape = shape
            density = 1f
            friction = 0.5f
            restitution = 0.1f
        }

        wallBody.createFixture(fixtureDef)
        shape.dispose()
    }
}
