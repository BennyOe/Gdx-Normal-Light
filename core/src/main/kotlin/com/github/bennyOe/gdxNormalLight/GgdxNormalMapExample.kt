package com.github.bennyOe.gdxNormalLight

import com.github.bennyOe.gdxNormalLight.demo.LightDemo
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.KtxAsync

class GgdxNormalMapExample : KtxGame<KtxScreen>() {
    override fun create() {
        KtxAsync.initiate()

        addScreen(LightDemo())
        setScreen<LightDemo>()
    }
}



