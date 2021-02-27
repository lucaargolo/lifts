package io.github.lucaargolo.lifts.client.screen

import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import io.github.lucaargolo.lifts.network.PacketCompendium
import io.github.lucaargolo.lifts.utils.LateTooltipHolder
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.AbstractButtonWidget
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper

class FloorSelectionScreen(val blockEntity: LiftBlockEntity): Screen(TranslatableText("screen.lifts.title.floor_selection")) {

    private val scrollTexture = Identifier("textures/gui/container/villager2.png")
    private val buttonLiftReference = linkedMapOf<LiftBlockEntity, ButtonWidget>()
    private val heightBtnReference = linkedMapOf<ButtonWidget, Int>()
    private val tooltipBtnReference = linkedMapOf<ButtonWidget, Text>()

    private var scrollableOffset = 0.0
    private var scrollable = false
    private var excessHeight = 0.0

    override fun init() {
        this.buttons.clear()
        buttonLiftReference.clear()
        heightBtnReference.clear()

        scrollableOffset = 0.0
        excessHeight = 0.0
        scrollable = false

        this.blockEntity.liftShaft?.lifts?.forEachIndexed { index, lift ->
            val btn = ButtonWidget(10, 10+(index*20), if(scrollable) 100 else 108, 20, getFloorName(index, lift), {
                val buf = PacketByteBufs.create()
                buf.writeBlockPos(lift.pos)
                ClientPlayNetworking.send(PacketCompendium.SEND_PLATFORM_ENTITY, buf)
            }, { button, _, _, _ ->
                tooltipBtnReference[button]?.let { LateTooltipHolder.scheduleLateTooltip(it, LateTooltipHolder.TooltipMode.RED) }
            })
            this.addButton(btn)
            if(btn.y + btn.height > 118) {
                if(!scrollable) {
                    scrollable = true
                    this.buttons.forEach {
                        it.width = 100
                    }
                    excessHeight += (btn.y + btn.height) - 118
                }else{
                    excessHeight += btn.height
                }
            }
            heightBtnReference[btn] = btn.y
            buttonLiftReference[lift] = btn
        }

    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if(scrollable && mouseX in (110.0..118.0) && mouseY in (10.0..118.0) && button == 0) {
            scrollableOffset = MathHelper.lerp((mouseY-10)/108, 0.0, excessHeight)
            updateButtonsHeight()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        if(scrollable) {
            scrollableOffset -= amount*4
            scrollableOffset = MathHelper.clamp(scrollableOffset, 0.0, excessHeight)
            updateButtonsHeight()
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, amount)
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        if(scrollable) {
            buttons.forEach { btn ->
                if((-20..120).contains(btn.y)) {
                    btn.render(matrices, mouseX, mouseY, delta)
                }
            }
            client?.textureManager?.bindTexture(scrollTexture)
            drawTexture(matrices, 110, 10, 93f, 17f, 8, 107, 512, 256)
            drawTexture(matrices, 110, 117, 93f, 158f, 8, 1, 512, 256)
            DrawableHelper.fill(matrices, 0, 118, 128, 128, 0xFF000000.toInt())
            DrawableHelper.fill(matrices, 0, 0, 128, 10, 0xFF000000.toInt())
            val offset = MathHelper.lerp(scrollableOffset/excessHeight, 0.0, 99.0)
            drawTexture(matrices, 111, 11+offset.toInt(), 0f, 199f, 6, 6, 512, 256)
            drawTexture(matrices, 111, 17+offset.toInt(), 0f, 225f, 6, 1, 512, 256)
        }else{
            super.render(matrices, mouseX, mouseY, delta)
        }
    }

    override fun tick() {
        if(this.buttons.size != blockEntity.liftShaft?.size ?: 0) {
            this.init()
        }
        var index = 0
        buttonLiftReference.forEach { (lift, btn) ->
            if(lift.isPlatformHere) {
                btn.active = false
                tooltipBtnReference[btn] = TranslatableText("screen.lifts.tooltip.already_here")
            }else{
                val actionResult = client?.world?.let { world -> blockEntity.liftShaft?.sendPlatformTo(world, lift, true) }
                if(actionResult?.isAccepted() == false) {
                    btn.active = false
                    tooltipBtnReference[btn] = TranslatableText("screen.lifts.tooltip.${actionResult.name.toLowerCase()}")
                }else{
                    btn.active = true
                    tooltipBtnReference.remove(btn)
                }
            }
            btn.message = getFloorName(index++, lift)
        }
    }

    private fun updateButtonsHeight() {
        this.buttons.forEach {
            it.y = (heightBtnReference[it] ?: 0) - scrollableOffset.toInt()
        }
    }

    private fun getFloorName(index: Int, lift: LiftBlockEntity): Text{
        val floor = this.blockEntity.liftShaft!!.size - index
        val suffix = when(floor) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
        return lift.liftName?.let { LiteralText(it) } ?: LiteralText("$floor").append(TranslatableText("screen.lifts.number.$suffix")).append(" ").append(TranslatableText("screen.lifts.common.floor"))
    }



}