package io.github.lucaargolo.lifts.common.block.screen

import io.github.lucaargolo.lifts.common.blockentity.screen.ScreenBlockEntity
import net.minecraft.block.*
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView

class ScreenBlock(settings: Settings): BlockWithEntity(settings) {

    override fun createBlockEntity(world: BlockView?) = ScreenBlockEntity()

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {

        builder.add(Properties.HORIZONTAL_FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.HORIZONTAL_FACING, ctx.playerFacing.opposite)
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

    override fun getOutlineShape(state: BlockState, view: BlockView?, pos: BlockPos?, ePos: ShapeContext?) = getShape(state[Properties.HORIZONTAL_FACING])

    override fun getCollisionShape(state: BlockState, view: BlockView?, pos: BlockPos?, ePos: ShapeContext?) = getShape(state[Properties.HORIZONTAL_FACING])

    companion object {
        private val EMPTY = createCuboidShape(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        private val SHAPES = mutableMapOf<Direction, VoxelShape>()

        init {
            Direction.values().forEach {
                when(it) {
                    Direction.EAST -> SHAPES[it] = createCuboidShape(0.0, 0.0, 0.0, 3.0, 16.0, 16.0)
                    Direction.WEST -> SHAPES[it] = createCuboidShape(13.0, 0.0, 0.0, 16.0, 16.0, 16.0)
                    Direction.SOUTH -> SHAPES[it] = createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 3.0)
                    Direction.NORTH -> SHAPES[it] = createCuboidShape(0.0, 0.0, 13.0, 16.0, 16.0, 16.0)
                    else -> {}
                }
            }
        }

        private fun getShape(facing: Direction) = SHAPES[facing] ?: EMPTY
    }

}