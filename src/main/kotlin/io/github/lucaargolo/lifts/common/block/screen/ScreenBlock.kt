package io.github.lucaargolo.lifts.common.block.screen

import io.github.lucaargolo.lifts.common.blockentity.screen.ScreenBlockEntity
import io.github.lucaargolo.lifts.common.item.wrench.LinkingTool
import net.minecraft.block.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World

class ScreenBlock(settings: Settings): BlockWithEntity(settings) {

    override fun createBlockEntity(world: BlockView?) = ScreenBlockEntity()

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.HORIZONTAL_FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.HORIZONTAL_FACING, ctx.playerFacing.opposite)
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

    override fun getOutlineShape(state: BlockState, view: BlockView?, pos: BlockPos?, ePos: ShapeContext?): VoxelShape = getShape(state[Properties.HORIZONTAL_FACING])

    override fun getCollisionShape(state: BlockState, view: BlockView?, pos: BlockPos?, ePos: ShapeContext?): VoxelShape = EMPTY

    @Suppress("DEPRECATION")
    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if(player.getStackInHand(hand).item is LinkingTool) {
            return ActionResult.PASS
        }
        val facing = state[Properties.HORIZONTAL_FACING]
        (world.getBlockEntity(pos) as? ScreenBlockEntity)?.let{ screenBlockEntity ->
            val mousePos = ScreenBlockHandler.getMousePosition(hit, facing, pos)
            if(screenBlockEntity.clickDelay == 0 && (mousePos.first != 0.0 || mousePos.second != 0.0)) {
                screenBlockEntity.clickDelay = 5
                screenBlockEntity.screen?.mouseClicked(mousePos.first, mousePos.second, 0)
            }
            return ActionResult.SUCCESS
        }
        return super.onUse(state, world, pos, player, hand, hit)
    }

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