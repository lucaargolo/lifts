package io.github.lucaargolo.lifts.common.containers.lift

import io.github.lucaargolo.lifts.common.blockentity.lift.StirlingLiftBlockEntity
import io.github.lucaargolo.lifts.common.containers.ScreenHandlerCompendium
import net.minecraft.block.entity.AbstractFurnaceBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot

class StirlingLiftScreenHandler(syncId: Int, playerInventory: PlayerInventory, val entity: StirlingLiftBlockEntity?, private val context: ScreenHandlerContext): ScreenHandler(ScreenHandlerCompendium.STIRLING_LIFT_TYPE, syncId)  {

    init {
        checkSize(entity, 1)
        entity?.onOpen(playerInventory.player)

        addSlot(object: Slot(entity, 0, 8+18*4, 18) {
            override fun canInsert(stack: ItemStack) = AbstractFurnaceBlockEntity.canUseAsFuel(stack)
        })

        (0..2).forEach { n ->
            (0..8).forEach { m ->
                addSlot(Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 49 + n*18))
            }
        }

        (0..8).forEach { n ->
            addSlot(Slot(playerInventory, n, 8 + n * 18, 107))
        }
    }

    override fun transferSlot(player: PlayerEntity?, invSlot: Int): ItemStack? {
        var itemStack = ItemStack.EMPTY
        val slot = this.slots[invSlot]
        if (slot != null && slot.hasStack()) {
            val itemStack2 = slot.stack
            itemStack = itemStack2.copy()
            if (invSlot < 1) {
                if (!insertItem(itemStack2, 1, this.slots.size, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!insertItem(itemStack2, 0, 1, false)) {
                return ItemStack.EMPTY
            }
            if (itemStack2.isEmpty) {
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }
        }
        return itemStack
    }

    override fun canUse(player: PlayerEntity) = entity?.canPlayerUse(player) ?: false

}