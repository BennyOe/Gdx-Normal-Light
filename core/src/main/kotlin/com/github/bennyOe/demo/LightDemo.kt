package com.github.bennyOe.demo

import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.github.bennyOe.core.GameLight
import com.github.bennyOe.core.LightEngine
import com.github.bennyOe.core.LightType
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.graphics.use
import ktx.math.vec2

const val PPM = 100f

class Light : KtxScreen {
    //-------Scene2d------//
    private lateinit var cam: OrthographicCamera
    private lateinit var viewport: ExtendViewport
    private lateinit var stage: Stage
    private lateinit var lightEngine: LightEngine


    private lateinit var batch: SpriteBatch
    private lateinit var wall: Texture
    private lateinit var wallNormals: Texture

    // --------- Box 2d lights --------//
    private val world = World(vec2(0f, -9.81f), true)
    private val rayHandler = RayHandler(world)
    private lateinit var wallBody: Body
    private val debugRenderer = Box2DDebugRenderer()


    override fun show() {
        cam = OrthographicCamera(Gdx.graphics.width.toFloat() / PPM, Gdx.graphics.height.toFloat() / PPM)
        rayHandler.setBlurNum(3)
        RayHandler.useDiffuseLight(false)
        batch = SpriteBatch()
        lightEngine = LightEngine(rayHandler, world, cam, batch)
        lightEngine.setAmbientLight(Color(1f,0f,0f,1f))
        lightEngine.addLight(LightType.POINT, vec2(1f, 1f), Color(1f, 1f, 1f, 1f), 3f, vec2(1f, 1f))

        wall = Texture("wall.png")
        wallNormals = Texture("wall_normal.png")

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

        viewport = ExtendViewport(19f, 9f)
        stage = Stage(ScreenViewport(), SpriteBatch())

        val label = Label("Scene2D working", Label.LabelStyle(BitmapFont(), Color.WHITE))
        label.setPosition(20f, 20f)
        stage.addActor(label)
    }

    override fun resize(width: Int, height: Int) {
        lightEngine.resize(width, height)
        stage.viewport.update(width, height)
    }

    override fun render(delta: Float) {
        world.step(1 / 60f, 6, 2)
        println(Gdx.graphics.framesPerSecond)

        lightEngine.renderLights {
            wallNormals.bind(1)
            wall.bind(0)
            batch.draw(wall, 0f, 0f)
        }

//        stage.act(delta)
//        stage.draw()

        debugRenderer.render(world, cam.combined)
    }

    override fun dispose() {
        batch.disposeSafely()
        wall.disposeSafely()
        wallNormals.disposeSafely()
        lightEngine.dispose()
    }

    fun createWallBody(world: World, position: Vector2, size: Float) {
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
