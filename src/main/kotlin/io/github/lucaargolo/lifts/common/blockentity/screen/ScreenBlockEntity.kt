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
import team.reborn.energy.EnergyHolder
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyStorage
import team.reborn.energy.EnergyTier

class ScreenBlockEntity: SynchronizeableBlockEntity(BlockEntityCompendium.SCREEN_TYPE), Linkable, Tickable, EnergyStorage {

    enum class State {
        NO_ENERGY,
        UNLINKED,
        LINKED
    }

    var state = State.NO_ENERGY
        set(value) {
            screen = null
            field = value
            if(world?.isClient == false) {
                sync()
            }
        }

    var linkedPos: BlockPos? = null
    var storedEnergy = 0.0

    override fun link(blockPos: BlockPos): Boolean {
        if(world?.getBlockEntity(blockPos) is LiftBlockEntity) {
            linkedPos = blockPos
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
            when(state) {
                State.NO_ENERGY -> if(storedEnergy >= 100.0) state = State.LINKED
                State.UNLINKED, State.LINKED -> {
                    if(storedEnergy < 100.0) {
                        state = State.NO_ENERGY
                    }else{
                        storedEnergy--
                    }
                    if(state == State.UNLINKED && linkedPos != null) {
                        state = State.LINKED
                    }else if(state == State.LINKED && linkedPos?.let { world?.getBlockEntity(it) } !is LiftBlockEntity) {
                        linkedPos = null
                        state = State.UNLINKED
                    }
                }
            }
            tickDelay = 0
        }else{
            tickDelay++
        }
        screen?.tick()
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        tag.putDouble("storedEnergy", storedEnergy)
        linkedPos?.let { tag.putLong("linkedPos", it.asLong()) }
        tag.putString("state", state.name)
        return super.toTag(tag)
    }

    override fun fromTag(blockState: BlockState, tag: CompoundTag) {
        super.fromTag(blockState, tag)
        storedEnergy = tag.getDouble("storedEnergy")
        linkedPos = if(tag.contains("linkedPos")) {
            BlockPos.fromLong(tag.getLong("linkedPos"))
        } else { null }
        state = try {
            State.valueOf(tag.getString("state"))
        } catch(e: Exception) {
            e.printStackTrace()
            State.NO_ENERGY
        }
    }

    override fun getMaxStoredPower() = 16000.0

    override fun getTier() = EnergyTier.LOW

    override fun getStored(p0: EnergySide?) = storedEnergy

    override fun setStored(p0: Double) {
        storedEnergy = p0
    }

}