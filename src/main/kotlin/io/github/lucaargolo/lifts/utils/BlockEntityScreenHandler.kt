package io.github.lucaargolo.lifts.utils

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

open class BlockEntityScreenHandler<E: InventoryBlockEntity>(type: ScreenHandlerType<*>, syncId: Int, playerInventory: PlayerInventory, val entity: E, private val blockContext: ScreenHandlerContext): ScreenHandler(type, syncId) {

    override fun canUse(player: PlayerEntity): Boolean {
        return blockContext.run({ world: World, blockPos: BlockPos ->
            if (world.getBlockState(blockPos).block != entity.cachedState?.block) false
            else player.squaredDistanceTo(blockPos.x + .5, blockPos.y + .5, blockPos.z + .5) < 64.0
        }, true)
    }
}