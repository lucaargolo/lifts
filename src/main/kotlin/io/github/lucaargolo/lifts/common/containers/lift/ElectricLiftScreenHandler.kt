package io.github.lucaargolo.lifts.common.containers.lift

import io.github.lucaargolo.lifts.common.blockentity.lift.ElectricLiftBlockEntity
import io.github.lucaargolo.lifts.common.containers.ScreenHandlerCompendium
import io.github.lucaargolo.lifts.network.PacketCompendium
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ElectricLiftScreenHandler(syncId: Int, private val playerInventory: PlayerInventory, val entity: ElectricLiftBlockEntity, private val context: ScreenHandlerContext): ScreenHandler(ScreenHandlerCompendium.ELECTRIC_LIFT_TYPE, syncId)  {

    var energyStored = 0.0

    init {
        (0..2).forEach { n ->
            (0..8).forEach { m ->
                addSlot(Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 84 + n*18))
            }
        }

        (0..8).forEach { n ->
            addSlot(Slot(playerInventory, n, 8 + n * 18, 142))
        }
    }

    override fun sendContentUpdates() {
        (playerInventory.player as? ServerPlayerEntity)?.let { player ->
            if(entity.getStored(null) != energyStored) {
                val double = entity.getStored(null)
                val buf = PacketByteBufs.create()
                buf.writeDouble(double)
                ServerPlayNetworking.send(player, PacketCompendium.UPDATE_ELECTRIC_LIFT_SCREEN_HANDLER, buf)
                energyStored = double
            }
        }
        super.sendContentUpdates()
    }

    override fun canUse(player: PlayerEntity): Boolean {
        return context.run({ world: World, blockPos: BlockPos ->
            if (world.getBlockEntity(blockPos) != entity) false
            else player.squaredDistanceTo(
                blockPos.x + .5,
                blockPos.y + .5,
                blockPos.z + .5
            ) < 64.0
        }, true)
    }

}