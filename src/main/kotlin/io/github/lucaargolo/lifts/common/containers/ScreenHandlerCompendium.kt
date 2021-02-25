package io.github.lucaargolo.lifts.common.containers

import io.github.lucaargolo.lifts.client.screen.StirlingLiftScreen
import io.github.lucaargolo.lifts.common.blockentity.lift.StirlingLiftBlockEntity
import io.github.lucaargolo.lifts.common.containers.lift.StirlingLiftScreenHandler
import io.github.lucaargolo.lifts.utils.RegistryCompendium
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.registry.Registry

object ScreenHandlerCompendium: RegistryCompendium<ScreenHandlerType<*>>(Registry.SCREEN_HANDLER) {

    val STIRLING_LIFT_TYPE = register("stirling_lift", ExtendedScreenHandlerType { i, playerInventory, packetByteBuf ->
        val pos = packetByteBuf.readBlockPos()
        val player = playerInventory.player
        val world = player.world
        val be = world.getBlockEntity(pos) as StirlingLiftBlockEntity
        StirlingLiftScreenHandler(i, playerInventory, be)
    })

    @Suppress("UNCHECKED_CAST")
    fun onInitializeClient() {
        ScreenRegistry.register(STIRLING_LIFT_TYPE) { handler, playerInventory, title -> StirlingLiftScreen(handler as StirlingLiftScreenHandler, playerInventory, title) as HandledScreen<ScreenHandler>}
    }

}