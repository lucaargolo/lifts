package io.github.lucaargolo.lifts.common.block.misc

import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.common.blockentity.misc.LiftButtonBlockEntity
import io.github.lucaargolo.lifts.common.item.linking.LinkingTool
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.StoneButtonBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

class LiftButton(settings: Settings): StoneButtonBlock(settings), BlockEntityProvider {

    override fun createBlockEntity(blockPos: BlockPos, blockState: BlockState) = LiftButtonBlockEntity(blockPos, blockState)

    @Suppress("DEPRECATION")
    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if(player.getStackInHand(hand).item is LinkingTool) {
            return ActionResult.PASS
        }
        if(!state[POWERED] && !world.isClient) {
            (world.getBlockEntity(pos) as? LiftButtonBlockEntity)?.let { be ->
                val linkedLift = be.linkedLift
                if(linkedLift == null || linkedLift.isRemoved) {
                    be.linkedLift = null
                    player.sendMessage(TranslatableText("message.lifts.button.not_linked").formatted(Formatting.RED), true)
                }else {
                    if(linkedLift.isPlatformHere) {
                        player.sendMessage(TranslatableText("message.lifts.button.lift_already_here").formatted(Formatting.RED), true)
                    }else{
                        linkedLift.liftShaft?.sendPlatformTo(world, linkedLift, false)?.let {
                            if(!it.isAccepted()) {
                                player.sendMessage(TranslatableText("screen.lifts.tooltip.${it.name.toLowerCase()}").formatted(Formatting.RED), true)
                            }
                        }
                    }
                }
            }
        }
        return super.onUse(state, world, pos, player, hand, hit)
    }

    override fun getWeakRedstonePower(state: BlockState, world: BlockView?, pos: BlockPos?, direction: Direction?) = 0

    override fun getStrongRedstonePower(state: BlockState, world: BlockView?, pos: BlockPos?, direction: Direction) = 0

    override fun emitsRedstonePower(state: BlockState?) = false

    override fun <T : BlockEntity?> getTicker(world: World?, state: BlockState?, type: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return if(type != BlockEntityCompendium.LIFT_BUTTON_TYPE) null else BlockEntityTicker { wrld, poss, stat, blockEntity ->
            LiftButtonBlockEntity.commonTick(wrld, poss, stat, blockEntity as LiftButtonBlockEntity)
        }
    }

}