package io.github.lucaargolo.lifts.client.screen

import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import io.github.lucaargolo.lifts.network.PacketCompendium
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.LiteralText

class LiftScreen(val blockEntity: LiftBlockEntity): Screen(LiteralText("Lift")) {

    private val buttonLiftReference = linkedMapOf<LiftBlockEntity, ButtonWidget>()

    override fun init() {
        this.buttons.clear()
        buttonLiftReference.clear()
        this.blockEntity.liftShaft?.sortedByDescending { it.pos }?.forEachIndexed { index, lift ->
            val btn = ButtonWidget(10, 10+(index*20), 108, 20, LiteralText(index.toString())) {
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
        buttonLiftReference.forEach { (lift, btn) ->
            btn.active = !lift.isPlatformHere
        }

    }



}