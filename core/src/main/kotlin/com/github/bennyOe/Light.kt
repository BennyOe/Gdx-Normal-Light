package com.github.bennyOe

import box2dLight.ConeLight
import box2dLight.PointLight
import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.Vector4
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.graphics.use
import ktx.math.vec2
import ktx.math.vec3
import ktx.math.vec4
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import kotlin.math.acos
import kotlin.math.cos

const val PPM = 100f

class Light : KtxScreen {
    private lateinit var batch: SpriteBatch
    private lateinit var cam: OrthographicCamera
    private lateinit var wall: Texture
    private lateinit var wallNormals: Texture
    private lateinit var shader: ShaderProgram

    private var normalInfluenceValue = 1.0f
    private var coneAngleValue = cos(toRadians(20.0)).toFloat()
    private var lightType: LightType = LightType.DIRECTIONAL
    private var lightIntensity: Float = 1f
    private var r = 1f
    private var g = 1f
    private var b = 1f

    private val lightPos = vec3(0f, 0f, 0f)
    private var lightColor = vec4(r, g, b, lightIntensity)
    private val ambientColor = vec4(0.6f, 0.6f, 1f, 0.04f)
    private val falloff = vec3(.4f, 3f, 20f)
    private val vertShader: FileHandle = Gdx.files.internal("shader/light.vert")
    private val fragShader: FileHandle = Gdx.files.internal("shader/light.frag")
    private val activeLights: MutableList<LightData> = mutableListOf()

    // --------- Box 2d lights --------//
    private val world = World(vec2(0f, -9.81f), true)
    private val rayHandler = RayHandler(world)
    private lateinit var pointLight: ConeLight
    private lateinit var wallBody: Body
    private val debugRenderer = Box2DDebugRenderer()


    override fun show() {
        rayHandler.setShadows(true)
        rayHandler.setAmbientLight(0f, 0f, 0f, 0.5f)
        rayHandler.setBlurNum(3)
        createWallBody(world)

        // ---- END BOX2d LIGHTS-------//
        activeLights.add(
            LightData(
                lightType,
                lightPos,
                vec3(0f, -1f, 0f),
                lightColor,
                ambientColor,
                falloff,
                coneAngleValue
            ),
        )

        wall = Texture("wall.png")
        wallNormals = Texture("wall_normal.png")

        ShaderProgram.pedantic = false
        shader = ShaderProgram(vertShader, fragShader)

        if (!shader.isCompiled) {
            throw GdxRuntimeException("Could not compile shader: ${shader.log}")
        }

        shader.bind()
        shader.setUniformi("LightCount", activeLights.size)

        shader.setUniformi("u_normals", 1)
        coneAngleValue = cos(toRadians(20.0)).toFloat()
        shader.setUniformf("normalInfluence", 1.0f)

        for (i in activeLights.indices) {
            val light = activeLights[i]
            val prefix = "[$i]"
            shader.setUniformi("LightType$prefix", light.type.ordinal)
            shader.setUniformf("LightPos$prefix", light.position)
            shader.setUniformf("LightDir$prefix", light.direction)
            shader.setUniformf("LightColor$prefix", light.color)
            shader.setUniformf("AmbientColor$prefix", light.ambient)
            shader.setUniformf("Falloff$prefix", light.falloff)
            shader.setUniformf("ConeAngle$prefix", light.coneAngle)
        }

        batch = SpriteBatch(1000)
        batch.shader = null

        cam = OrthographicCamera( Gdx.graphics.width.toFloat() / PPM, Gdx.graphics.height.toFloat() / PPM)
        val centerX = cam.position.x
        val centerY = cam.position.y
        pointLight = ConeLight(rayHandler, 128, Color(1f, 1f, 1f, 1f), 8f, centerX +2f , centerY +3f, 240f, 40f)

        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun scrolled(amountX: Float, amountY: Float): Boolean {
                val angleDeg = toDegrees(acos(coneAngleValue.toDouble())) + amountY
                coneAngleValue = cos(toRadians(angleDeg.coerceIn(1.0, 89.0))).toFloat()
                return true
            }
        }
    }

    override fun resize(width: Int, height: Int) {
        cam.setToOrtho(false, width / PPM, height / PPM)

        // ZENTRIEREN auf Weltmitte!
        cam.position.set(
            0f,
            0f,
            0f
        )

        cam.update()
        batch.projectionMatrix = cam.combined
        shader.bind()
        shader.setUniformf("Resolution", width.toFloat(), height.toFloat())
        rayHandler.setCombinedMatrix(cam)
    }

    override fun render(delta: Float) {
        world.step(1/60f, 6, 2)
        println(Gdx.graphics.framesPerSecond)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

//        batch.use {
//            val x = Gdx.input.x.toFloat() / Gdx.graphics.width
//            val y = 1f - Gdx.input.y.toFloat() / Gdx.graphics.height
//
//            lightPos.set(x, y, 0f)
//            shader.setUniformf("LightPos", lightPos)
//
//            val lightDir = vec3(-1f, -1f, 0f)
//            shader.setUniformf("LightDir", lightDir)
//
//            keyboardInput()
//
//            wallNormals.bind(1)
//            wall.bind(0)
//
//            batch.draw(wall, 0f, 0f)
//        }
        //------ Box 2d lights ------//
        cam.update()
        debugRenderer.render(world, cam.combined)
        rayHandler.setShadows(true)
        rayHandler.setCombinedMatrix(cam)
        rayHandler.updateAndRender()
    }

    private fun keyboardInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            normalInfluenceValue = (normalInfluenceValue - 0.01f).coerceAtLeast(0f)
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            normalInfluenceValue = (normalInfluenceValue + 0.01f).coerceAtMost(1f)
        }
        shader.setUniformf("normalInfluence", normalInfluenceValue)

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            lightIntensity += 0.05f
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            lightIntensity -= 0.05f
        }
        if (Gdx.input.isKeyPressed(Input.Keys.R)) {
            r = 1f
            g = 0f
            b = 0f
        } else if (Gdx.input.isKeyPressed(Input.Keys.G)) {
            r = 0f
            g = 1f
            b = 0f
        } else if (Gdx.input.isKeyPressed(Input.Keys.B)) {
            r = 0f
            g = 0f
            b = 1f
        } else if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            r = 1f
            g = 1f
            b = 1f
        }
        lightColor = vec4(r, g, b, lightIntensity)
        shader.setUniformf("LightColor", lightColor)

        if (Gdx.input.isKeyPressed((Input.Keys.NUM_1))) {
            lightType = LightType.DIRECTIONAL
        }
        if (Gdx.input.isKeyPressed((Input.Keys.NUM_2))) {
            lightType = LightType.POINT
        }
        if (Gdx.input.isKeyPressed((Input.Keys.NUM_3))) {
            lightType = LightType.SPOT
        }
        shader.setUniformi("LightType", lightType.ordinal)

    }

    override fun dispose() {
        batch.disposeSafely()
        wall.disposeSafely()
        wallNormals.disposeSafely()
        shader.disposeSafely()
    }

    fun createWallBody(world: World) {
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.StaticBody
            position.set(0f, 0f) // sichtbar im Zentrum
        }

        wallBody = world.createBody(bodyDef)

        val shape = PolygonShape().apply {
            setAsBox(1f, 1f, vec2(0f, 0f), 0f) // große Fläche = sichtbar, zentriert
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

enum class LightType {
    DIRECTIONAL, POINT, SPOT
}

data class LightData(
    val type: LightType,
    val position: Vector3,
    val direction: Vector3,
    val color: Vector4,
    val ambient: Vector4,
    val falloff: Vector3,
    val coneAngle: Float
)
