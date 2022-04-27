package io.github.lucaargolo.lifts.common.containers

import io.github.lucaargolo.lifts.client.screen.ElectricLiftScreen
import io.github.lucaargolo.lifts.client.screen.StirlingLiftScreen
import io.github.lucaargolo.lifts.common.blockentity.lift.ElectricLiftBlockEntity
import io.github.lucaargolo.lifts.common.blockentity.lift.StirlingLiftBlockEntity
import io.github.lucaargolo.lifts.common.containers.lift.ElectricLiftScreenHandler
import io.github.lucaargolo.lifts.common.containers.lift.StirlingLiftScreenHandler
import io.github.lucaargolo.lifts.utils.RegistryCompendium
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.screen.ScreenHandlerContext
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

    val ELECTRIC_LIFT_TYPE = register("electric_lift", ExtendedScreenHandlerType { i, playerInventory, packetByteBuf ->
        val pos = packetByteBuf.readBlockPos()
        val player = playerInventory.player
        val world = player.world
        val be = world.getBlockEntity(pos) as ElectricLiftBlockEntity
        ElectricLiftScreenHandler(i, playerInventory, be, ScreenHandlerContext.create(world, pos))
    })

    @Suppress("UNCHECKED_CAST")
    fun onInitializeClient() {
        HandledScreens.register(STIRLING_LIFT_TYPE, ::StirlingLiftScreen)
        HandledScreens.register(ELECTRIC_LIFT_TYPE, ::ElectricLiftScreen)
    }

}