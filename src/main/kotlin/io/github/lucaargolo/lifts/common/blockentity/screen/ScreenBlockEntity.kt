package io.github.lucaargolo.lifts.common.blockentity.screen

import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import io.github.lucaargolo.lifts.utils.Linkable
import io.github.lucaargolo.lifts.utils.SynchronizeableBlockEntity
import net.minecraft.block.BlockState
import net.minecraft.client.gui.screen.Screen
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos

class ScreenBlockEntity: SynchronizeableBlockEntity(BlockEntityCompendium.SCREEN_TYPE), Linkable, Tickable {

    enum class State {
        NO_ENERGY,
        UNLINKED,
        LINKED
    }

    var state = State.UNLINKED
        set(value) {
            screen = null
            field = value
        }

    var linkedPos: BlockPos? = null

    override fun link(blockPos: BlockPos): Boolean {
        if(world?.getBlockEntity(blockPos) is LiftBlockEntity) {
            linkedPos = blockPos
            state = State.LINKED
            return true
        }
        return false
    }

    var screen: Screen? = null
    var clickDelay = 0
    var tickDelay = 0

    override fun tick() {
        if(clickDelay > 0) {
            clickDelay--
        }
        if(tickDelay > 20) {
            if(state == State.LINKED && linkedPos?.let { world?.getBlockEntity(it) } !is LiftBlockEntity) {
                linkedPos = null
                state = State.UNLINKED
            }
            tickDelay = 0
        }else{
            tickDelay++
        }
        screen?.tick()
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        linkedPos?.let { tag.putLong("linkedPos", it.asLong()) }
        tag.putString("state", state.name)
        return super.toTag(tag)
    }

    override fun fromTag(blockState: BlockState, tag: CompoundTag) {
        super.fromTag(blockState, tag)
        linkedPos = if(tag.contains("linkedPos")) {
            BlockPos.fromLong(tag.getLong("linkedPos"))
        } else { null }
        state = try {
            State.valueOf(tag.getString("state"))
        } catch(e: Exception) {
            e.printStackTrace()
            State.NO_ENERGY
        }
        println(state.name)
    }

}