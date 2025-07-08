package com.github.bennyOe.gdxNormalLight.demo

import com.badlogic.gdx.ApplicationListener
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync

class GgdxNormalMapExample : KtxGame<KtxScreen>(), ApplicationListener {
    override fun create() {
        KtxAsync.initiate()

        addScreen(LightDemo())
        setScreen<LightDemo>()
    }
}
