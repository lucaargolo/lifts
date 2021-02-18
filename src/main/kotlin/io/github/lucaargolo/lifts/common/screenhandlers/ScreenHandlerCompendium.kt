package io.github.lucaargolo.lifts.common.screenhandlers

import io.github.lucaargolo.lifts.common.blockentity.screen.ScreenBlockEntity
import io.github.lucaargolo.lifts.common.screenhandlers.screen.ScreenScreenHandler
import io.github.lucaargolo.lifts.utils.RegistryCompendium
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.registry.Registry

object ScreenHandlerCompendium: RegistryCompendium<ScreenHandlerType<*>>(Registry.SCREEN_HANDLER) {

    val SCREEN_HANDLER_TYPE = ExtendedScreenHandlerType { i, playerInventory, packetByteBuf ->
        val pos = packetByteBuf.readBlockPos()
        val player = playerInventory.player
        val world = player.world
        val be = world.getBlockEntity(pos) as ScreenBlockEntity
        ScreenScreenHandler(i, playerInventory, be, ScreenHandlerContext.create(world, pos))
    }

}