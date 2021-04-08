package io.github.lucaargolo.lifts.utils

import net.minecraft.client.render.BackgroundRenderer
import net.minecraft.client.render.Camera

object BackgroundRendererCache {

    var camera: Camera? = null
    var fogType: BackgroundRenderer.FogType? = null
    var viewDistance: Float? = null
    var thickFog: Boolean? = null

    fun restoreCache() {
        if(camera != null && fogType != null && viewDistance != null && thickFog != null) {
            BackgroundRenderer.applyFog(camera, fogType, viewDistance!!, thickFog!!)
        }
    }

}