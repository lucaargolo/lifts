package io.github.lucaargolo.lifts.common.blockentity.charger

import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.common.blockentity.screen.ScreenBlockEntity
import io.github.lucaargolo.lifts.utils.LinkActionResult
import io.github.lucaargolo.lifts.utils.Linkable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtDouble
import net.minecraft.nbt.NbtLong
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import team.reborn.energy.api.base.SimpleEnergyStorage

class ChargerBlockEntity(pos: BlockPos, state: BlockState): BlockEntity(BlockEntityCompendium.SCREEN_CHARGER_TYPE, pos, state), Linkable {

    private var longArray: LongArray? = null
    private val linkedScreens = linkedSetOf<ScreenBlockEntity>()

    @Suppress("UnstableApiUsage")
    val energyStorage = object: SimpleEnergyStorage(128000, 512, 0) {
        override fun onFinalCommit() {
            super.onFinalCommit()
            markDirty()
        }
    }

    override fun link(blockPos: BlockPos): LinkActionResult {
        return (world?.getBlockEntity(blockPos) as? ScreenBlockEntity)?.let {
            val distance = MathHelper.sqrt(blockPos.getSquaredDistance(pos.x+0.0, pos.y+0.0, pos.z+0.0, true).toFloat())
            if(distance > MAX_SCREEN_DISTANCE) {
                LinkActionResult.TOO_FAR_AWAY
            }else {
                linkedScreens.add(it)
                LinkActionResult.SUCCESSFUL
            }
        } ?: LinkActionResult.NOT_SCREEN
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        tag.putLong("storedEnergy", energyStorage.amount)
        val longArray = linkedScreens.map { it.pos.asLong() }.toLongArray()
        tag.putLongArray("linkedScreens", longArray)
        return super.writeNbt(tag)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        val storedEnergy = tag.get("storedEnergy") ?: NbtLong.of(0L)
        when(storedEnergy.nbtType) {
            NbtLong.TYPE -> energyStorage.amount = tag.getLong("storedEnergy")
            NbtDouble.TYPE -> energyStorage.amount = MathHelper.floor(tag.getDouble("storedEnergy")).toLong()
        }
        longArray = tag.getLongArray("linkedScreens")
    }

    companion object {
        const val MAX_SCREEN_DISTANCE = 128

        @Suppress("unused_parameter")
        fun commonTick(world: World, pos: BlockPos, state: BlockState, entity: ChargerBlockEntity) {
            entity.longArray?.forEach {
                entity.link(BlockPos.fromLong(it))
                entity.longArray = null
            }
            val iterator = entity.linkedScreens.iterator()
            val splitEnergy = (entity.energyStorage.amount/entity.linkedScreens.size).coerceAtMost(32)
            while(iterator.hasNext()) {
                val screen = iterator.next()
                if(screen.isRemoved) {
                    iterator.remove()
                }else{
                    val oldScreenStored = screen.energyStorage.amount
                    val screenMaxStored = screen.energyStorage.capacity
                    if(oldScreenStored + splitEnergy <= screenMaxStored) {
                        screen.energyStorage.amount = oldScreenStored+splitEnergy
                        entity.energyStorage.amount -= splitEnergy
                        entity.markDirty()
                    }else if(oldScreenStored < screenMaxStored) {
                        screen.energyStorage.amount = screenMaxStored
                        entity.energyStorage.amount -= (screenMaxStored - oldScreenStored)
                        entity.markDirty()
                    }
                }
            }
        }

    }

}