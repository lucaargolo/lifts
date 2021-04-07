package io.github.lucaargolo.lifts.common.block.lift

import io.github.lucaargolo.lifts.common.blockentity.lift.ElectricLiftBlockEntity
import io.github.lucaargolo.lifts.common.containers.lift.ElectricLiftScreenHandler
import io.github.lucaargolo.lifts.utils.ModConfig
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World
import team.reborn.energy.EnergyTier

class ElectricLift(settings: Settings, val electricLiftConfig: ModConfig.ElectricLiftConfig, val energyTier: EnergyTier): Lift(settings, electricLiftConfig) {

    override fun createBlockEntity(world: BlockView?) = ElectricLiftBlockEntity()

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        player.openHandledScreen(object: ExtendedScreenHandlerFactory {
            override fun getDisplayName() = TranslatableText("screen.lifts.title.electric_lift")

            override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
                return ElectricLiftScreenHandler(syncId, inv, world.getBlockEntity(pos) as ElectricLiftBlockEntity, ScreenHandlerContext.create(world, pos))
            }

            override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
                buf.writeBlockPos(pos)
            }
        })
        return ActionResult.SUCCESS
    }

}