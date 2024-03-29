@file:Suppress("DEPRECATION", "UnstableApiUsage")

package io.github.lucaargolo.lifts.common.blockentity.lift

import io.github.lucaargolo.lifts.Lifts
import io.github.lucaargolo.lifts.common.block.lift.ElectricLift
import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import team.reborn.energy.api.base.SimpleEnergyStorage

class ElectricLiftBlockEntity(pos: BlockPos, state: BlockState): LiftBlockEntity(BlockEntityCompendium.ELECTRIC_LIFT_TYPE, pos, state) {

    private var initializedEnergy = false
    private var liftMaxInsert = 0L
    private var liftCapacity = 0L

    val energyStorage = object: SimpleEnergyStorage(0, 0, 0) {
        private fun getMaxInsert(): Long {
            return liftMaxInsert
        }

        override fun getCapacity(): Long {
            return liftCapacity
        }

        override fun supportsInsertion(): Boolean {
            return getMaxInsert() > 0
        }

        override fun insert(maxAmount: Long, transaction: TransactionContext?): Long {
            StoragePreconditions.notNegative(maxAmount)
            val inserted = getMaxInsert().coerceAtMost(maxAmount.coerceAtMost(getCapacity() - amount))
            if (inserted > 0) {
                updateSnapshots(transaction)
                amount += inserted
                return inserted
            }
            return 0
        }

        override fun onFinalCommit() {
            super.onFinalCommit()
            markDirty()
        }
    }

    override fun getReachableLifts(): Int {
        var x = 0
        liftShaft?.lifts?.forEach {
            val distance = MathHelper.abs(it.pos.y - pos.y)
            lift?.liftConfig?.platformRange?.let { range ->
                if((0..range).contains(distance) && energyStorage.amount >= distance*ENERGY_PER_BLOCK) {
                    x++
                }
            }
        }
        return x
    }

    override fun preSendRequirements(distance: Int): LiftActionResult {
        val cost = ENERGY_PER_BLOCK * distance
        return if(energyStorage.amount - cost >= 0) LiftActionResult.SUCCESSFUL else LiftActionResult.NO_ENERGY
    }

    override fun postSendRequirements(distance: Int) {
        val cost = ENERGY_PER_BLOCK * distance
        energyStorage.amount -= cost
        markDirty()
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        if(tag.contains("energyStored")) {
            energyStorage.amount = MathHelper.floor(tag.getDouble("energyStored")).toLong()
        }else{
            energyStorage.amount = tag.getLong("storedEnergy")
        }
    }

    override fun writeNbt(tag: NbtCompound) {
        tag.putLong("storedEnergy", energyStorage.amount)
        super.writeNbt(tag)
    }

    companion object {
        val ENERGY_PER_BLOCK = Lifts.CONFIG.energyUnitsNeededPerBlock

        fun commonTick(world: World, pos: BlockPos, state: BlockState, entity: ElectricLiftBlockEntity) {
            LiftBlockEntity.commonTick(world, pos, state, entity)
            if(!entity.initializedEnergy) {
                (entity.lift as? ElectricLift)?.let {
                    entity.liftMaxInsert = it.liftMaxExtract
                    entity.liftCapacity = it.electricLiftConfig.energyCapacity
                    entity.initializedEnergy = true
                }
            }
        }
    }

}