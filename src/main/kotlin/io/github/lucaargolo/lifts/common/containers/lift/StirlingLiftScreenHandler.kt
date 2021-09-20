package io.github.lucaargolo.lifts.common.containers.lift

import io.github.lucaargolo.lifts.common.blockentity.lift.StirlingLiftBlockEntity
import io.github.lucaargolo.lifts.common.containers.ScreenHandlerCompendium
import net.fabricmc.fabric.api.registry.FuelRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot

class StirlingLiftScreenHandler(syncId: Int, playerInventory: PlayerInventory, val entity: StirlingLiftBlockEntity): ScreenHandler(ScreenHandlerCompendium.STIRLING_LIFT_TYPE, syncId)  {

    private val propertyDelegate = entity.propertyDelegate

    var burningTime
        get() = propertyDelegate.get(0)
        set(value) = propertyDelegate.set(0, value)

    var burningTicks
        get() = propertyDelegate.get(1)
        set(value) = propertyDelegate.set(1, value)

    var storedTicks
        get() = propertyDelegate.get(2)
        set(value) = propertyDelegate.set(2, value)

    init {
        checkSize(entity, 1)
        checkDataCount(propertyDelegate, 3)
        entity.onOpen(playerInventory.player)

        addSlot(object: Slot(entity, 0, 26, 53) {
            override fun canInsert(stack: ItemStack) = (FuelRegistry.INSTANCE.get(stack.item) ?: 0) > 0
        })

        (0..2).forEach { n ->
            (0..8).forEach { m ->
                addSlot(Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 84 + n*18))
            }
        }

        (0..8).forEach { n ->
            addSlot(Slot(playerInventory, n, 8 + n * 18, 142))
        }

        addProperties(propertyDelegate)
    }

    override fun transferSlot(player: PlayerEntity?, invSlot: Int): ItemStack? {
        var itemStack = ItemStack.EMPTY
        val slot = this.slots[invSlot]
        if (slot.hasStack()) {
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

    override fun canUse(player: PlayerEntity) = entity.canPlayerUse(player)

}