package io.github.lucaargolo.lifts.common.screenhandlers.screen

import io.github.lucaargolo.lifts.common.blockentity.screen.ScreenBlockEntity
import io.github.lucaargolo.lifts.common.screenhandlers.ScreenHandlerCompendium
import io.github.lucaargolo.lifts.utils.BlockEntityScreenHandler
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot

class ScreenScreenHandler(syncId: Int, playerInventory: PlayerInventory, entity: ScreenBlockEntity, blockContext: ScreenHandlerContext): BlockEntityScreenHandler<ScreenBlockEntity>(ScreenHandlerCompendium.SCREEN_HANDLER_TYPE, syncId, playerInventory, entity, blockContext) {

    init {
        checkSize(entity, 9)
        entity.onOpen(playerInventory.player)

        (0..8).forEach { n ->
            addSlot(Slot(entity, n, 8 + n * 18, 18))
        }

        (0..2).forEach { n ->
            (0..8).forEach { m ->
                addSlot(Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 49 + n*18))
            }
        }

        (0..8).forEach { n ->
            addSlot(Slot(playerInventory, n, 8 + n * 18, 107))
        }
    }

    override fun transferSlot(player: PlayerEntity?, index: Int): ItemStack? {
        var itemStack = ItemStack.EMPTY
        val slot = slots[index] as Slot
        if (slot.hasStack()) {
            val itemStack2 = slot.stack
            itemStack = itemStack2.copy()
            if (index < 9) {
                if (!insertItem(itemStack2, 9, slots.size, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!insertItem(itemStack2, 0, 9, false)) {
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

}