package io.github.lucaargolo.lifts.client.screen

import io.github.lucaargolo.lifts.common.containers.lift.StirlingLiftScreenHandler
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

class StirlingLiftScreen(handler: StirlingLiftScreenHandler, inventory: PlayerInventory, title: Text): HandledScreen<StirlingLiftScreenHandler>(handler, inventory, title) {

    private val texture = Identifier("lifts:textures/gui/stirling_lift.png")

    override fun init() {
        super.init()
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2
        this.addButton(ButtonWidget(x+61, y+51, 90, 20, TranslatableText("screen.common.rename_lift")) { MinecraftClient.getInstance().openScreen(RenameLiftScreen(handler.entity)) })
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
    }

    override fun drawForeground(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
        super.drawForeground(matrices, mouseX, mouseY)
        itemRenderer.renderInGui(ItemStack(handler.entity.lift), 26, 18)
        textRenderer.draw(matrices, TranslatableText("screen.common.name").append(": ${handler.entity.liftName ?: "Default"}"), 61f, 20f, 4210752)
        textRenderer.draw(matrices, TranslatableText("screen.common.burning_ticks").append(": ${handler.burningTicks}"), 61f, 30f, 4210752)
        textRenderer.draw(matrices, TranslatableText("screen.common.stored_ticks").append(": ${handler.storedTicks}"), 61f, 40f, 4210752)
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        client!!.textureManager.bindTexture(texture)
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight)
        if(handler.burningTicks > 0) {
            val progress = handler.burningTicks * 13 / if(handler.burningTime == 0) 200 else handler.burningTime
            this.drawTexture(matrices, x+27, y+37+12-progress, 176, 12-progress, 14, progress+1)
        }
    }

}