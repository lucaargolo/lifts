package io.github.lucaargolo.lifts.common.blockentity.screen

import io.github.lucaargolo.lifts.common.block.screen.ScreenBlockHandler
import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.utils.InventoryBlockEntity
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gui.screen.Screen
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Tickable
import net.minecraft.util.collection.DefaultedList

class ScreenBlockEntity: InventoryBlockEntity(BlockEntityCompendium.SCREEN_TYPE, 9), Tickable {

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