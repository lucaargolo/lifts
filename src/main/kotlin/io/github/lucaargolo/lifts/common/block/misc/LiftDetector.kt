package io.github.lucaargolo.lifts.common.block.misc

import io.github.lucaargolo.lifts.common.blockentity.misc.LiftDetectorBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView

class LiftDetector(settings: Settings): BlockWithEntity(settings)  {

    init {
        defaultState = defaultState.with(STATE, State.NOT_LINKED)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(STATE)
    }

    override fun createBlockEntity(world: BlockView?) = LiftDetectorBlockEntity()

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

    override fun getWeakRedstonePower(state: BlockState, world: BlockView?, pos: BlockPos?, direction: Direction?): Int {
        return if (state[STATE] == State.HERE) 15 else 0
    }

    override fun getStrongRedstonePower(state: BlockState, world: BlockView?, pos: BlockPos?, direction: Direction): Int {
        return if (state[STATE] == State.HERE) 15 else 0
    }

    override fun emitsRedstonePower(state: BlockState?) = true

    enum class State: StringIdentifiable {
        NOT_LINKED,
        NOT_HERE,
        HERE;

        override fun asString() = name.toLowerCase()
    }

    companion object {
        val STATE: EnumProperty<State> = EnumProperty.of("state", State::class.java)
    }

}