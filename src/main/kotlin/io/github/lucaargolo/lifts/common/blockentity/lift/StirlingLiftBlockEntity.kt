package io.github.lucaargolo.lifts.common.blockentity.lift

import io.github.lucaargolo.lifts.common.block.lift.Lift
import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import net.minecraft.block.BlockState
import net.minecraft.block.entity.AbstractFurnaceBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper

class StirlingLiftBlockEntity(lift: Lift?): LiftBlockEntity(BlockEntityCompendium.STIRLING_LIFT_TYPE, lift), SidedInventory {

    private var inventory = DefaultedList.ofSize(1, ItemStack.EMPTY)
    private var storedTicks = 0
    private var burningTicks = 0

    override fun clear() = inventory.clear()
    override fun size() = inventory.size
    override fun isEmpty() = !inventory.map { it.isEmpty }.contains(false)
    override fun getStack(slot: Int) = inventory[slot]

    override fun removeStack(slot: Int, amount: Int): ItemStack = Inventories.splitStack(this.inventory, slot, amount)
    override fun removeStack(slot: Int): ItemStack = Inventories.removeStack(this.inventory, slot)
    override fun setStack(slot: Int, stack: ItemStack) = let { this.inventory[slot] = stack }

    override fun getAvailableSlots(side: Direction?) = intArrayOf(0)
    override fun canInsert(slot: Int, stack: ItemStack?, dir: Direction?) = AbstractFurnaceBlockEntity.canUseAsFuel(stack)
    override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?) = false

    override fun tick() {
        if(burningTicks == 0) {
            if(storedTicks < MAX_FUEL_TICKS) {
                val fuelTime = getFuelTime(inventory[0])
                if(fuelTime > 0) {
                    inventory[0].decrement(1)
                    burningTicks += fuelTime
                }
            }
        }else{
            if(storedTicks < MAX_FUEL_TICKS) {
                storedTicks++
                burningTicks--
            }
        }
        super.tick()
    }

    private fun getFuelTime(fuel: ItemStack): Int {
        return if(fuel.isEmpty) 0 else AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(fuel.item, 0)
    }

    override fun sendPlatformTo(world: ServerWorld, destination: LiftBlockEntity): Boolean {
        val distance = MathHelper.abs(destination.pos.y - this.pos.y)
        val cost = TICKS_PER_BLOCK * distance
        if(storedTicks - cost >= 0) {
            val result = super.sendPlatformTo(world, destination)
            if(result) {
                storedTicks -= cost
                sync()
            }
            return result
        }
        return false
    }

    override fun canPlayerUse(player: PlayerEntity): Boolean {
        return if (world?.getBlockEntity(pos) !== this) {
            false
        } else {
            player.squaredDistanceTo(pos.x.toDouble() + 0.5, pos.y.toDouble() + 0.5, pos.z.toDouble() + 0.5) <= 64.0
        }
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag) {
        Inventories.fromTag(tag, inventory)
        storedTicks = tag.getInt("storedTicks")
        burningTicks = tag.getInt("burningTicks")
        super.fromTag(state, tag)
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        Inventories.toTag(tag, inventory)
        tag.putInt("storedTicks", storedTicks)
        tag.putInt("burningTicks", burningTicks)
        return super.toTag(tag)
    }

    companion object {
        const val MAX_FUEL_TICKS = 32000
        const val TICKS_PER_BLOCK = 100
    }

}