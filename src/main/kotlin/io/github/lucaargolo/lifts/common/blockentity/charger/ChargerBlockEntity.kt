package io.github.lucaargolo.lifts.common.blockentity.charger

import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import io.github.lucaargolo.lifts.common.blockentity.screen.ScreenBlockEntity
import io.github.lucaargolo.lifts.utils.LinkActionResult
import io.github.lucaargolo.lifts.utils.Linkable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyStorage
import team.reborn.energy.EnergyTier

class ChargerBlockEntity(pos: BlockPos, state: BlockState): BlockEntity(BlockEntityCompendium.SCREEN_CHARGER_TYPE, pos, state), Linkable, EnergyStorage {

    private var longArray: LongArray? = null
    private val linkedScreens = linkedSetOf<ScreenBlockEntity>()
    private var storedEnergy = 0.0

    override fun link(blockPos: BlockPos): LinkActionResult {
        return (world?.getBlockEntity(blockPos) as? ScreenBlockEntity)?.let {
            val distance = MathHelper.sqrt(blockPos.getSquaredDistance(pos.x+0.0, pos.y+0.0, pos.z+0.0, true))
            if(distance > MAX_SCREEN_DISTANCE) {
                LinkActionResult.TOO_FAR_AWAY
            }else {
                linkedScreens.add(it)
                LinkActionResult.SUCCESSFUL
            }
        } ?: LinkActionResult.NOT_SCREEN
    }

    override fun getMaxStoredPower() = 128000.0

    override fun getMaxOutput(side: EnergySide?) = 0.0

    override fun getTier() = EnergyTier.HIGH

    override fun getStored(face: EnergySide?) = storedEnergy

    override fun setStored(amount: Double) {
        storedEnergy = amount
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        tag.putDouble("storedEnergy", storedEnergy)
        val longArray = linkedScreens.map { it.pos.asLong() }.toLongArray()
        tag.putLongArray("linkedScreens", longArray)
        return super.writeNbt(tag)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        storedEnergy = tag.getDouble("storedEnergy")
        longArray = tag.getLongArray("linkedScreens")
    }

    companion object {
        const val MAX_SCREEN_DISTANCE = 128

        fun commonTick(world: World, pos: BlockPos, state: BlockState, entity: ChargerBlockEntity) {
            entity.longArray?.forEach {
                entity.link(BlockPos.fromLong(it))
                entity.longArray = null
            }
            val iterator = entity.linkedScreens.iterator()
            val splitEnergy = (entity.storedEnergy/entity.linkedScreens.size).coerceAtMost(32.0)
            while(iterator.hasNext()) {
                val screen = iterator.next()
                if(screen.isRemoved) {
                    iterator.remove()
                }else{
                    val oldScreenStored = screen.getStored(null)
                    val screenMaxStored = screen.maxStoredPower
                    if(oldScreenStored + splitEnergy <= screenMaxStored) {
                        screen.setStored(oldScreenStored+splitEnergy)
                        entity.storedEnergy -= splitEnergy
                        entity.markDirty()
                    }else if(oldScreenStored < screenMaxStored) {
                        screen.setStored(screenMaxStored)
                        entity.storedEnergy -= (screenMaxStored - oldScreenStored)
                        entity.markDirty()
                    }
                }
            }
        }

    }

}