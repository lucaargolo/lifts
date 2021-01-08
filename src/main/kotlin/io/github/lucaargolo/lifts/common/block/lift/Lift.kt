package io.github.lucaargolo.lifts.common.block.lift

import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.world.BlockView

class Lift(settings: Settings): BlockWithEntity(settings) {

    override fun createBlockEntity(world: BlockView?) = LiftBlockEntity(this)

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.HORIZONTAL_FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.HORIZONTAL_FACING, ctx.playerFacing.opposite)
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

}