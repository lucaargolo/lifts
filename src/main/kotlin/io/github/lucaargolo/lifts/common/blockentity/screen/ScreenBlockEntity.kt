package io.github.lucaargolo.lifts.common.blockentity.screen

import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import io.github.lucaargolo.lifts.utils.LinkActionResult
import io.github.lucaargolo.lifts.utils.Linkable
import io.github.lucaargolo.lifts.utils.SynchronizeableBlockEntity
import net.minecraft.block.BlockState
import net.minecraft.client.gui.screen.Screen
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyStorage
import team.reborn.energy.EnergyTier

class ScreenBlockEntity(pos: BlockPos, state: BlockState): SynchronizeableBlockEntity(BlockEntityCompendium.SCREEN_TYPE, pos, state), Linkable, EnergyStorage {

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

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        tag.putDouble("storedEnergy", storedEnergy)
        linkedLift?.let { tag.putLong("linkedLift", it.pos.asLong()) }
        linkedPos?.let { tag.putLong("linkedLift", it.asLong()) }
        tag.putString("state", state.name)
        return super.writeNbt(tag)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
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

        fun commonTick(world: World, pos: BlockPos, state: BlockState, entity: ScreenBlockEntity) {
            entity.linkedPos?.let {
                entity.linkedLift = world.getBlockEntity(entity.linkedPos) as? LiftBlockEntity
                entity.linkedPos = null
            }
            if(entity.clickDelay > 0) {
                entity.clickDelay--
            }
            entity.screen?.tick()
            if(world.isClient) return
            if(entity.tickDelay > 20) {
                when(entity.state) {
                    State.NO_ENERGY -> if(entity.storedEnergy >= 100.0) entity.state = State.LINKED
                    State.UNLINKED, State.LINKED -> {
                        if(entity.storedEnergy < 100.0) {
                            entity.state = State.NO_ENERGY
                        }else{
                            entity.storedEnergy--
                        }
                        if(entity.state == State.UNLINKED && entity.linkedLift != null) {
                            entity.state = State.LINKED
                        }else if(entity.state == State.LINKED && (entity.linkedLift == null || entity.linkedLift?.isRemoved == true)) {
                            entity.linkedLift = null
                            entity.state = State.UNLINKED
                        }
                    }
                }
                entity.markDirty()
                entity.tickDelay = 0
            }else{
                entity.tickDelay++
                if(entity.state != State.NO_ENERGY && entity.storedEnergy > 0.0) {
                    entity.storedEnergy--
                    entity.markDirty()
                }
            }
        }
    }

}