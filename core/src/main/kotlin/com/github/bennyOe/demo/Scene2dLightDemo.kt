package com.github.bennyOe.demo

import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.bennyOe.core.LightEngine
import com.github.bennyOe.core.LightType
import com.github.bennyOe.scene2d.LightActor
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.math.vec2
import ktx.math.vec3


class Scene2dLightDemo : KtxScreen {
    //-------Scene2d------//
    private lateinit var cam: OrthographicCamera
    private lateinit var viewport: Viewport
    private lateinit var stage: Stage
    private lateinit var lightEngine: LightEngine
    private lateinit var actor: LightActor


    private lateinit var batch: SpriteBatch
    private lateinit var wall: Texture
    private lateinit var wallNormals: Texture

    // --------- Box 2d lights --------//
    private val world = World(vec2(0f, -9.81f), true)
    private val rayHandler = RayHandler(world)
    private lateinit var wallBody: Body
    private val debugRenderer = Box2DDebugRenderer()


    override fun show() {
        cam = OrthographicCamera()
        rayHandler.setBlurNum(3)
        RayHandler.useDiffuseLight(false)
        batch = SpriteBatch()
        viewport = FitViewport(19f, 9f, cam)
        stage = Stage(viewport, batch)
        wall = Texture("wall.png")
        wallNormals = Texture("wall_normal.png")

        lightEngine = LightEngine(rayHandler, cam, batch, viewport)

//        val spotlight = lightEngine.addLight(
//            type = LightType.SPOT,
//            position = vec2(9.5f, 4.5f),
//            color = Color(1f, 0.7f, 0.1f, 1f),
//            spotAngle = 50f,
//            direction = 0f,
//            intensity = 14f,
//        )

//        lightEngine.addLight(
//            type = LightType.DIRECTIONAL,
//            color = Color(0f,1f,0f,0.4f),
//            position = vec2(0f,0f),
//            direction = 0f,
//            intensity = 0.05f
//        )

        val light = lightEngine.addLight(
            LightType.POINT,
            Vector2(-9.5f, 5f),
            Color.RED,
            0f,
            8f,
            vec3(2f,2f,2f)
        )

//        val light1 = lightEngine.addLight(
//            LightType.POINT,
//            vec2(3f, 3f),
//            Color(1f, 0f, 1f, 1f),
//            0f,
//            2f,
//        )

        actor = LightActor(light)
        stage.addActor(actor)

        createWalls()


        val label = Label("Scene2D working", Label.LabelStyle(BitmapFont(), Color.WHITE))
        label.setPosition(20f, 20f)
        stage.addActor(label)
    }

    override fun resize(width: Int, height: Int) {
        lightEngine.resize(width, height)
        viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        world.step(1 / 60f, 6, 2)

        cam.update()
        viewport.apply()

        val mouse = Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
        val worldPos = viewport.unproject(mouse)
        actor.setPosition(worldPos.x, worldPos.y)
        stage.act(delta)

        batch.projectionMatrix = cam.combined
        lightEngine.update()
        lightEngine.renderLights {
            wallNormals.bind(1)
            wall.bind(0)
            batch.draw(wall, 0f, 0f, 19f, 9f)
        }
        stage.draw()

        debugRenderer.render(world, cam.combined)
    }

    override fun dispose() {
        batch.disposeSafely()
        wall.disposeSafely()
        wallNormals.disposeSafely()
        lightEngine.dispose()
    }

    private fun createWalls() {
        // Wände am Rand der Welt
        val wallSize = 0.5f

        // Ecken
        createWallBody(world, vec2(wallSize, wallSize), wallSize)
        createWallBody(world, vec2(19 - wallSize, wallSize), wallSize)
        createWallBody(world, vec2(wallSize, 9 - wallSize), wallSize)
        createWallBody(world, vec2(19 - wallSize, 9 - wallSize), wallSize)

        // Seitenmitte
        createWallBody(world, vec2(9.5f, wallSize), wallSize)
        createWallBody(world, vec2(9.5f, 9 - wallSize), wallSize)
        createWallBody(world, vec2(wallSize, 4.5f), wallSize)
        createWallBody(world, vec2(19 - wallSize, 4.5f), wallSize)

        // Zusätzliche Wände in der Mitte
        createWallBody(world, vec2(5f, 4.5f), wallSize)
        createWallBody(world, vec2(14f, 4.5f), wallSize)
        createWallBody(world, vec2(9.5f, 2.5f), wallSize)
        createWallBody(world, vec2(9.5f, 6.5f), wallSize)
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
