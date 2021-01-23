package io.github.lucaargolo.lifts.common.blockentity.screen

import io.github.lucaargolo.lifts.common.block.screen.ScreenBlockHandler
import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gui.screen.Screen
import net.minecraft.util.Tickable

class ScreenBlockEntity: BlockEntity(BlockEntityCompendium.SCREEN_TYPE), Tickable {

    val isScreenSetup
        get() = screen != null

    var screen: Screen? = null
    var clickDelay = 0

    fun setupScreen(screen: Screen) {
        screen.init(MinecraftClient.getInstance(), ScreenBlockHandler.getFramebufferHeight(), ScreenBlockHandler.getFramebufferWidth())
        this.screen = screen
    }

    override fun tick() {
        if(clickDelay > 0) {
            clickDelay--
        }
        screen?.tick()
    }

}