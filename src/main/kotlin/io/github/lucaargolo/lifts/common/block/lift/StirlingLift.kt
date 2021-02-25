package io.github.lucaargolo.lifts.common.block.lift

import io.github.lucaargolo.lifts.common.blockentity.lift.StirlingLiftBlockEntity
import io.github.lucaargolo.lifts.common.containers.lift.StirlingLiftScreenHandler
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World

class StirlingLift(settings: Settings, platformSpeed: Double, platformRange: Int): Lift(settings, platformSpeed, platformRange) {

    override fun createBlockEntity(world: BlockView?) = StirlingLiftBlockEntity()

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        player.openHandledScreen(object: ExtendedScreenHandlerFactory {
            override fun getDisplayName() = TranslatableText("screen.title.stirling_lift")

            override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
                return StirlingLiftScreenHandler(syncId, inv, world.getBlockEntity(pos) as StirlingLiftBlockEntity)
            }

            override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
                buf.writeBlockPos(pos)
            }
        })
        return ActionResult.SUCCESS
    }

}