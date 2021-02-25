package io.github.lucaargolo.lifts.common.blockentity.lift

import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import net.fabricmc.fabric.api.registry.FuelRegistry
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.PropertyDelegate
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World

class StirlingLiftBlockEntity: LiftBlockEntity(BlockEntityCompendium.STIRLING_LIFT_TYPE), SidedInventory {

    private var inventory = DefaultedList.ofSize(1, ItemStack.EMPTY)
    private var burningTime = 0
    private var burningTicks = 0
    private var storedTicks = 0

    val propertyDelegate = object: PropertyDelegate {
        override fun get(index: Int): Int {
            return when(index) {
                0 -> burningTime
                1 -> burningTicks
                2 -> storedTicks
                else -> 0
            }
        }

        override fun set(index: Int, value: Int) {
            when(index) {
                0 -> burningTime
                1 -> burningTicks = value
                2 -> storedTicks = value
            }
        }

        override fun size() = 3

    }

    override fun clear() = inventory.clear()
    override fun size() = inventory.size
    override fun isEmpty() = !inventory.map { it.isEmpty }.contains(false)
    override fun getStack(slot: Int) = inventory[slot]

    override fun removeStack(slot: Int, amount: Int): ItemStack = Inventories.splitStack(this.inventory, slot, amount)
    override fun removeStack(slot: Int): ItemStack = Inventories.removeStack(this.inventory, slot)
    override fun setStack(slot: Int, stack: ItemStack) = let { this.inventory[slot] = stack }

    override fun getAvailableSlots(side: Direction?) = intArrayOf(0)
    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?) = FuelRegistry.INSTANCE.get(stack.item) > 0
    override fun canExtract(slot: Int, stack: ItemStack, dir: Direction?) = false

    override fun tick() {
        if(burningTicks == 0) {
            if(storedTicks < MAX_FUEL_TICKS) {
                burningTime = getFuelTime(inventory[0])
                if(burningTime > 0) {
                    inventory[0].decrement(1)
                    burningTicks += burningTime
                }
            }
        }else{
            if(storedTicks < MAX_FUEL_TICKS) {
                storedTicks++
                burningTicks--
                if(burningTicks == 0) {
                    burningTime = 0
                }
            }
        }
        super.tick()
    }

    private fun getFuelTime(fuel: ItemStack): Int {
        return if(fuel.isEmpty) 0 else FuelRegistry.INSTANCE.get(fuel.item)
    }

    override fun sendPlatformTo(world: World, destination: LiftBlockEntity, simulation: Boolean): LiftActionResult {
        val distance = MathHelper.abs(destination.pos.y - this.pos.y)
        val cost = TICKS_PER_BLOCK * distance
        if(storedTicks - cost >= 0) {
            val result = super.sendPlatformTo(world, destination, simulation)
            if(result.isAccepted() && !simulation) {
                storedTicks -= cost
                sync()
            }
            return result
        }
        return LiftActionResult.NO_FUEL
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