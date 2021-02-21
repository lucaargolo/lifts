package io.github.lucaargolo.lifts.common.block.lift

import io.github.lucaargolo.lifts.client.screen.LiftScreen
import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World

class Lift(settings: Settings): BlockWithEntity(settings) {

    override fun createBlockEntity(world: BlockView?) = LiftBlockEntity(this)

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if(world.isClient) {
            (world.getBlockEntity(pos) as? LiftBlockEntity)?.let {
                MinecraftClient.getInstance().openScreen(LiftScreen(it))
            }
        }
        return ActionResult.SUCCESS
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.HORIZONTAL_FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.HORIZONTAL_FACING, ctx.playerFacing.opposite)
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

    override fun hasComparatorOutput(state: BlockState?) = true

    override fun getComparatorOutput(state: BlockState?, world: World, pos: BlockPos?): Int {
        (world.getBlockEntity(pos) as? LiftBlockEntity)?.let{
            if(it.isPlatformHere && it.isShaftValid) return 15
        }
        return 0
    }

}