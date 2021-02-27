package io.github.lucaargolo.lifts.common.blockentity.lift

import io.github.lucaargolo.lifts.common.block.lift.ElectricLift
import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.MathHelper
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyStorage
import team.reborn.energy.EnergyTier

class ElectricLiftBlockEntity: LiftBlockEntity(BlockEntityCompendium.ELECTRIC_LIFT_TYPE), EnergyStorage {

    private var initializedEnergy = false
    private var energyTier = EnergyTier.INSANE
    private var energyCapacity = 0.0
    private var energyStored = 0.0

    override fun getMaxOutput(side: EnergySide?) = 0.0

    override fun getMaxStoredPower() = this.energyCapacity

    override fun getTier() = this.energyTier

    override fun getStored(energySide: EnergySide?): Double {
        return this.energyStored
    }

    override fun setStored(energyStored: Double) {
        this.energyStored = energyStored
        markDirty()
    }

    override fun getReachableLifts(): Int {
        var x = 0
        liftShaft?.lifts?.forEach {
            val distance = MathHelper.abs(it.pos.y - pos.y)
            lift?.platformRange?.let { range ->
                if((0..range).contains(distance) && energyStored >= distance*ENERGY_PER_BLOCK) {
                    x++
                }
            }
        }
        return x
    }

    override fun tick() {
        super.tick()
        if(!initializedEnergy) {
            (lift as? ElectricLift)?.let {
                energyTier = it.energyTier
                energyCapacity = it.energyCapacity
                initializedEnergy = true
            }
        }
    }

    override fun preSendRequirements(distance: Int): LiftActionResult {
        val cost = ENERGY_PER_BLOCK * distance
        return if(energyStored - cost >= 0) LiftActionResult.SUCCESSFUL else LiftActionResult.NO_ENERGY
    }

    override fun postSendRequirements(distance: Int) {
        val cost = ENERGY_PER_BLOCK * distance
        energyStored -= cost
        markDirty()
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