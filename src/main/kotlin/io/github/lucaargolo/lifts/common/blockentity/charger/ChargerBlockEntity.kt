package io.github.lucaargolo.lifts.common.blockentity.charger

import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.common.blockentity.screen.ScreenBlockEntity
import io.github.lucaargolo.lifts.utils.LinkActionResult
import io.github.lucaargolo.lifts.utils.Linkable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyStorage
import team.reborn.energy.EnergyTier

class ChargerBlockEntity: BlockEntity(BlockEntityCompendium.SCREEN_CHARGER_TYPE), Linkable, Tickable, EnergyStorage {

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

    override fun tick() {
        longArray?.forEach {
            link(BlockPos.fromLong(it))
            longArray = null
        }
        val iterator = linkedScreens.iterator()
        val splitEnergy = (storedEnergy/linkedScreens.size).coerceAtMost(32.0)
        while(iterator.hasNext()) {
            val screen = iterator.next()
            if(screen.isRemoved) {
                iterator.remove()
            }else{
                val oldScreenStored = screen.getStored(null)
                val screenMaxStored = screen.maxStoredPower
                if(oldScreenStored + splitEnergy <= screenMaxStored) {
                    screen.setStored(oldScreenStored+splitEnergy)
                    storedEnergy -= splitEnergy
                    markDirty()
                }else if(oldScreenStored < screenMaxStored) {
                    screen.setStored(screenMaxStored)
                    storedEnergy -= (screenMaxStored - oldScreenStored)
                    markDirty()
                }
            }
        }
    }

    override fun getMaxStoredPower() = 128000.0

    override fun getMaxOutput(side: EnergySide?) = 0.0

    override fun getTier() = EnergyTier.HIGH

    override fun getStored(face: EnergySide?) = storedEnergy

    override fun setStored(amount: Double) {
        storedEnergy = amount
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        tag.putDouble("storedEnergy", storedEnergy)
        val longArray = linkedScreens.map { it.pos.asLong() }.toLongArray()
        tag.putLongArray("linkedScreens", longArray)
        return super.toTag(tag)
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag) {
        super.fromTag(state, tag)
        storedEnergy = tag.getDouble("storedEnergy")
        longArray = tag.getLongArray("linkedScreens")
    }

    companion object {
        const val MAX_SCREEN_DISTANCE = 128
    }

}