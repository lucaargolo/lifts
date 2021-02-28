package io.github.lucaargolo.lifts.common.blockentity.screen

import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.common.blockentity.charger.ChargerBlockEntity
import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import io.github.lucaargolo.lifts.utils.LinkActionResult
import io.github.lucaargolo.lifts.utils.Linkable
import io.github.lucaargolo.lifts.utils.SynchronizeableBlockEntity
import net.minecraft.block.BlockState
import net.minecraft.client.gui.screen.Screen
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
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

    private var storedEnergy = 0.0

    var state = State.NO_ENERGY
        set(value) {
            screen = null
            field = value
            if(world?.isClient == false) {
                sync()
            }
        }

    private var linkedPos: BlockPos? = null
    var linkedLift: LiftBlockEntity? = null

    override fun link(blockPos: BlockPos): LinkActionResult{
        return (world?.getBlockEntity(blockPos) as? LiftBlockEntity)?.let {
            val distance = MathHelper.sqrt(blockPos.getSquaredDistance(pos.x+0.0, pos.y+0.0, pos.z+0.0, true))
            if(distance > MAX_LIFT_DISTANCE) {
                LinkActionResult.TOO_FAR_AWAY
            }else {
                linkedLift = it
                LinkActionResult.SUCCESSFUL
            }
        } ?: LinkActionResult.NOT_LIFT
    }

    var screen: Screen? = null
    var clickDelay = 0
    var tickDelay = 0

    override fun tick() {
        linkedPos?.let {
            linkedLift = world?.getBlockEntity(linkedPos) as? LiftBlockEntity
            linkedPos = null
        }
        if(clickDelay > 0) {
            clickDelay--
        }
        screen?.tick()
        if(world?.isClient == true) return
        if(tickDelay > 20) {
            when(state) {
                State.NO_ENERGY -> if(storedEnergy >= 100.0) state = State.LINKED
                State.UNLINKED, State.LINKED -> {
                    if(storedEnergy < 100.0) {
                        state = State.NO_ENERGY
                    }else{
                        storedEnergy--
                    }
                    if(state == State.UNLINKED && linkedLift != null) {
                        state = State.LINKED
                    }else if(state == State.LINKED && (linkedLift == null || linkedLift?.isRemoved == true)) {
                        linkedLift = null
                        state = State.UNLINKED
                    }
                }
            }
            markDirty()
            tickDelay = 0
        }else{
            tickDelay++
            if(state != State.NO_ENERGY && storedEnergy > 0.0) {
                storedEnergy--
                markDirty()
            }
        }
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        tag.putDouble("storedEnergy", storedEnergy)
        linkedLift?.let { tag.putLong("linkedLift", it.pos.asLong()) }
        linkedPos?.let { tag.putLong("linkedLift", it.asLong()) }
        tag.putString("state", state.name)
        return super.toTag(tag)
    }

    override fun fromTag(blockState: BlockState, tag: CompoundTag) {
        super.fromTag(blockState, tag)
        storedEnergy = tag.getDouble("storedEnergy")
        linkedPos = if(tag.contains("linkedLift")) {
            BlockPos.fromLong(tag.getLong("linkedLift"))
        } else { null }
        state = try {
            State.valueOf(tag.getString("state"))
        } catch(e: Exception) {
            e.printStackTrace()
            State.NO_ENERGY
        }
    }

    override fun getMaxStoredPower() = 16000.0

    override fun getMaxOutput(side: EnergySide?) = 0.0

    override fun getTier() = EnergyTier.LOW

    override fun getStored(side: EnergySide?) = storedEnergy

    override fun setStored(storedEnergy: Double) {
        this.storedEnergy = storedEnergy
    }

    companion object {
        const val MAX_LIFT_DISTANCE = 32
    }

}