package com.github.bennyOe.demo

import box2dLight.RayHandler
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
import com.badlogic.gdx.utils.viewport.FillViewport
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.bennyOe.core.AbstractLightEngine
import com.github.bennyOe.core.Falloff
import com.github.bennyOe.core.GameLight
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.math.vec2
import ktx.math.vec3

abstract class AbstractLightDemo : KtxScreen {
    protected lateinit var cam: OrthographicCamera
    protected lateinit var viewport: Viewport
    protected lateinit var batch: SpriteBatch
    protected lateinit var wall: Texture
    protected lateinit var wallNormals: Texture

    protected val world = World(vec2(0f, -9.81f), true)
    protected val rayHandler = RayHandler(world)
    protected lateinit var wallBody: Body
    protected val debugRenderer = Box2DDebugRenderer()

    override fun show() {
        cam = OrthographicCamera()
        rayHandler.setBlurNum(6)
        batch = SpriteBatch()
        viewport = ExtendViewport(19f, 9f, cam)
        wall = Texture("wall.png")
        wallNormals = Texture("wall_normal.png")

        createWalls()
    }

    protected fun createWalls() {
        val wallSize = 0.5f

        // Corners
        createWallBody(world, vec2(wallSize, wallSize), wallSize)
        createWallBody(world, vec2(19 - wallSize, wallSize), wallSize)
        createWallBody(world, vec2(wallSize, 9 - wallSize), wallSize)
        createWallBody(world, vec2(19 - wallSize, 9 - wallSize), wallSize)

        // Side-Mid
        createWallBody(world, vec2(9.5f, wallSize), wallSize)
        createWallBody(world, vec2(9.5f, 9 - wallSize), wallSize)
        createWallBody(world, vec2(wallSize, 4.5f), wallSize)
        createWallBody(world, vec2(19 - wallSize, 4.5f), wallSize)

        // Additional walls in the middle
        createWallBody(world, vec2(5f, 4.5f), wallSize)
        createWallBody(world, vec2(14f, 4.5f), wallSize)
        createWallBody(world, vec2(9.5f, 2.5f), wallSize)
        createWallBody(world, vec2(9.5f, 6.5f), wallSize)
    }

    protected fun createWallBody(world: World, position: Vector2, size: Float) {
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.StaticBody
            position.set(position)
        }

        wallBody = world.createBody(bodyDef)

        val shape = PolygonShape().apply {
            setAsBox(size, size, position, 0f)
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

//    protected fun createLights(lightEngine: AbstractLightEngine): GameLight {
//        lightEngine.setAmbientLight(Color(1f, 1f, 1f, 0.2f))

//        return lightEngine.addDirectionalLight(Color(1f, 0.3f, 0.5f, 0.5f), 0f, 1f, 0.3f)


//        return lightEngine.addSpotLight(
//            vec2(6f, 4f),
//            Color(1f, 0f, 1f, 1f),
//            0f,
//            80f,
//            9f
//        )
//    }

    override fun dispose() {
        batch.disposeSafely()
        wall.disposeSafely()
        wallNormals.disposeSafely()
    }
}
