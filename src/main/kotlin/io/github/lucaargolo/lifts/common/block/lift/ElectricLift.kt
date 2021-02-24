package io.github.lucaargolo.lifts.common.block.lift

import io.github.lucaargolo.lifts.common.blockentity.lift.ElectricLiftBlockEntity
import net.minecraft.world.BlockView
import team.reborn.energy.EnergyTier

class ElectricLift(settings: Settings, platformSpeed: Double, platformRange: Int, private val energyCapacity: Double, private val energyTier: EnergyTier): Lift(settings, platformSpeed, platformRange) {

    override fun createBlockEntity(world: BlockView?) = ElectricLiftBlockEntity(this, energyCapacity, energyTier)

}