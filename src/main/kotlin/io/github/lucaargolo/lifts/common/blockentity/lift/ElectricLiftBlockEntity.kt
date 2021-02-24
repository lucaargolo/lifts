package io.github.lucaargolo.lifts.common.blockentity.lift

import io.github.lucaargolo.lifts.common.block.lift.Lift
import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.MathHelper
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyStorage
import team.reborn.energy.EnergyTier

class ElectricLiftBlockEntity(lift: Lift?, private val energyCapacity: Double, private val energyTier: EnergyTier): LiftBlockEntity(BlockEntityCompendium.ELECTRIC_LIFT_TYPE, lift), EnergyStorage {

    private var energyStored = 0.0

    override fun getMaxStoredPower() = this.energyCapacity

    override fun getTier() = this.energyTier

    override fun getStored(energySide: EnergySide?): Double {
        return this.energyStored
    }

    override fun setStored(energyStored: Double) {
        this.energyStored = energyStored
    }

    override fun sendPlatformTo(world: ServerWorld, destination: LiftBlockEntity): Boolean {
        val distance = MathHelper.abs(destination.pos.y - this.pos.y)
        val cost = ENERGY_PER_BLOCK * distance
        if(energyStored - cost >= 0) {
            val result = super.sendPlatformTo(world, destination)
            if(result) {
                energyStored -= cost
                sync()
            }
            return result
        }
        return false
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag) {
        super.fromTag(state, tag)
        energyStored = tag.getDouble("energyStored")
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        tag.putDouble("energyStored", energyStored)
        return super.toTag(tag)
    }

    companion object {
        const val ENERGY_PER_BLOCK = 100.0
    }

}