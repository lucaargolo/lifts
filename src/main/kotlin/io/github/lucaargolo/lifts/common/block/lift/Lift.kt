package io.github.lucaargolo.lifts.common.block.lift

import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

abstract class Lift(settings: Settings, val platformSpeed: Double, val platformRange: Int): BlockWithEntity(settings) {

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.HORIZONTAL_FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.HORIZONTAL_FACING, ctx.playerFacing.opposite)
    }

    @Suppress("DEPRECATION")
    override fun neighborUpdate(state: BlockState?, world: World, pos: BlockPos, block: Block?, fromPos: BlockPos?, notify: Boolean) {
        (world.getBlockEntity(pos) as? LiftBlockEntity)?.let{
            it.resetPlatformCache()
            it.liftShaft?.updateLift(it)
        }
        super.neighborUpdate(state, world, pos, block, fromPos, notify)
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

    override fun hasComparatorOutput(state: BlockState?) = true

    override fun getComparatorOutput(state: BlockState?, world: World, pos: BlockPos?): Int {
        (world.getBlockEntity(pos) as? LiftBlockEntity)?.let{
            if(it.isPlatformHere) return 15
        }
        return 0
    }

}