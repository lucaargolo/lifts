package io.github.lucaargolo.lifts.client.screen

import io.github.lucaargolo.lifts.common.containers.lift.ElectricLiftScreenHandler
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper

class ElectricLiftScreen(handler: ElectricLiftScreenHandler, inventory: PlayerInventory, title: Text): HandledScreen<ElectricLiftScreenHandler>(handler, inventory, title) {

    private val texture = Identifier("lifts:textures/gui/electric_lift.png")

    override fun init() {
        super.init()
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2
        this.addButton(ButtonWidget(x+61, y+51, 90, 20, TranslatableText("screen.common.rename_lift")) { MinecraftClient.getInstance().openScreen(RenameLiftScreen(handler.entity)) })
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
        if((x+8..x+16).contains(mouseX) && (y+17..y+69).contains(mouseY)) {
            val a = TranslatableText("screen.common.stored_energy").append(": ")
            val b = LiteralText("${handler.energyStored}/${handler.entity.maxStoredPower} E")
            renderTooltip(matrices, listOf(a, b), mouseX, mouseY)
        }
    }

    override fun drawForeground(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
        super.drawForeground(matrices, mouseX, mouseY)
        itemRenderer.renderInGui(ItemStack(handler.entity.lift), 26, 18)
        textRenderer.draw(matrices, TranslatableText("screen.common.name").append(": ${handler.entity.liftName ?: "Default"}"), 61f, 20f, 4210752)
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        client!!.textureManager.bindTexture(texture)
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight)
        val energyPercentage = handler.energyStored/handler.entity.maxStoredPower
        val energyOffset = MathHelper.lerp(energyPercentage, 0.0, 52.0).toInt()
        drawTexture(matrices, x+8, y+17+(52-energyOffset), 176, 0, 8, energyOffset)
    }

}