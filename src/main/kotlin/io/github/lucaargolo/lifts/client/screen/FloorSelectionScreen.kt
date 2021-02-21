package io.github.lucaargolo.lifts.client.screen

import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import io.github.lucaargolo.lifts.network.PacketCompendium
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.LiteralText

class FloorSelectionScreen(val blockEntity: LiftBlockEntity): Screen(LiteralText("Lift")) {

    private val buttonLiftReference = linkedMapOf<LiftBlockEntity, ButtonWidget>()

    override fun init() {
        this.buttons.clear()
        buttonLiftReference.clear()
        this.blockEntity.liftShaft?.sortedByDescending { it.pos }?.forEachIndexed { index, lift ->
            val btn = ButtonWidget(10, 10+(index*20), 108, 20, LiteralText(getFloorName(index, lift))) {
                val buf = PacketByteBufs.create()
                buf.writeBlockPos(lift.pos)
                ClientPlayNetworking.send(PacketCompendium.SEND_PLATFORM_ENTITY, buf)
            }
            this.addButton(btn)
            buttonLiftReference[lift] = btn
        }

    }

    override fun tick() {
        if(this.buttons.size != blockEntity.liftShaft?.size ?: 0) {
            this.init()
        }
        var index = 0
        buttonLiftReference.forEach { (lift, btn) ->
            btn.active = !lift.isPlatformHere
            btn.message = LiteralText(getFloorName(index++, lift))
        }

    }

    private fun getFloorName(index: Int, lift: LiftBlockEntity): String {
        val floor = this.blockEntity.liftShaft!!.size - index
        val suffix = when(floor) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
        return lift.liftName ?: "$floor$suffix Floor"
    }



}