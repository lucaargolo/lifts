package io.github.lucaargolo.lifts.utils

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.Direction

open class InventoryBlockEntity(type: BlockEntityType<*>, size: Int): SynchronizeableBlockEntity(type), SidedInventory {

    val inventory = DefaultedList.ofSize(size, ItemStack.EMPTY)
    private val availableSlots = (0 until size).toList().toIntArray()

    override fun size() = inventory.size

    override fun isEmpty() = inventory.all { it.isEmpty }

    override fun clear()  = inventory.clear()

    override fun setStack(slot: Int, stack: ItemStack?) {
        inventory[slot] = stack
        if (stack!!.count > maxCountPerStack) {
            stack.count = maxCountPerStack
        }
    }

    override fun getStack(slot: Int) = inventory[slot]

    override fun removeStack(slot: Int, amount: Int): ItemStack = Inventories.splitStack(inventory, slot, amount)

    override fun removeStack(slot: Int): ItemStack = Inventories.removeStack(this.inventory, slot)

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return if (world!!.getBlockEntity(pos) != this) {
            false
        } else {
            player!!.squaredDistanceTo(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5) <= 64.0
        }
    }

    override fun getAvailableSlots(side: Direction?) = availableSlots

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?) = stack.item is BlockItem && (stack.item as BlockItem).block.defaultState.isFullCube(world, pos)

    override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?) = true

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        Inventories.fromTag(tag, inventory)
        super.fromTag(state, tag)
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        Inventories.toTag(tag, inventory)
        return super.toTag(tag)
    }


}