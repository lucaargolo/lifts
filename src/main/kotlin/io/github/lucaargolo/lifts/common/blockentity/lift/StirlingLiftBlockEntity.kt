package io.github.lucaargolo.lifts.common.blockentity.lift

import io.github.lucaargolo.lifts.Lifts
import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import net.fabricmc.fabric.api.registry.FuelRegistry
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.PropertyDelegate
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World

class StirlingLiftBlockEntity(pos: BlockPos, state: BlockState): LiftBlockEntity(BlockEntityCompendium.STIRLING_LIFT_TYPE, pos, state), SidedInventory {

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
    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?) = FuelRegistry.INSTANCE.get(stack.item) ?: 0 > 0
    override fun canExtract(slot: Int, stack: ItemStack, dir: Direction?) = false

    override fun getReachableLifts(): Int {
        var x = 0
        liftShaft?.lifts?.forEach {
            val distance = MathHelper.abs(it.pos.y - pos.y)
            lift?.liftConfig?.platformRange?.let { range ->
                if((0..range).contains(distance) && storedTicks >= distance * TICKS_PER_BLOCK) {
                    x++
                }
            }
        }
        return x
    }

    private fun getFuelTime(fuel: ItemStack): Int {
        return if(fuel.isEmpty) 0 else FuelRegistry.INSTANCE.get(fuel.item) ?: 0
    }

    override fun preSendRequirements(distance: Int): LiftActionResult {
        val cost = TICKS_PER_BLOCK * distance
        return if(storedTicks - cost >= 0) LiftActionResult.SUCCESSFUL else LiftActionResult.NO_FUEL
    }

    override fun postSendRequirements(distance: Int) {
        val cost = TICKS_PER_BLOCK * distance
        storedTicks -= cost
        markDirty()
    }

    override fun canPlayerUse(player: PlayerEntity): Boolean {
        return if (world?.getBlockEntity(pos) !== this) {
            false
        } else {
            player.squaredDistanceTo(pos.x.toDouble() + 0.5, pos.y.toDouble() + 0.5, pos.z.toDouble() + 0.5) <= 64.0
        }
    }

    override fun readNbt(tag: NbtCompound) {
        Inventories.readNbt(tag, inventory)
        storedTicks = tag.getInt("storedTicks")
        burningTicks = tag.getInt("burningTicks")
        super.readNbt(tag)
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        Inventories.writeNbt(tag, inventory)
        tag.putInt("storedTicks", storedTicks)
        tag.putInt("burningTicks", burningTicks)
        return super.writeNbt(tag)
    }

    companion object {
        val MAX_FUEL_TICKS = Lifts.CONFIG.maxFuelTicksStored
        val TICKS_PER_BLOCK = Lifts.CONFIG.fuelTicksNeededPerBlock

        fun commonTick(world: World, pos: BlockPos, state: BlockState, entity: StirlingLiftBlockEntity) {
            if(entity.burningTicks == 0) {
                if(entity.storedTicks < MAX_FUEL_TICKS) {
                    entity.burningTime = entity.getFuelTime(entity.inventory[0])
                    if(entity.burningTime > 0) {
                        entity.inventory[0].decrement(1)
                        entity.burningTicks += entity.burningTime
                    }
                    entity.markDirty()
                }
            }else{
                if(entity.storedTicks < MAX_FUEL_TICKS) {
                    entity.storedTicks++
                    entity.burningTicks--
                    if(entity.burningTicks == 0) {
                        entity.burningTime = 0
                    }
                    entity.markDirty()
                }
            }
            LiftBlockEntity.commonTick(world, pos, state, entity)
        }
    }

}