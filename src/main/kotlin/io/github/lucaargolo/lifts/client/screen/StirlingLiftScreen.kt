package io.github.lucaargolo.lifts.client.screen

import io.github.lucaargolo.lifts.common.containers.lift.StirlingLiftScreenHandler
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class StirlingLiftScreen(handler: StirlingLiftScreenHandler, inventory: PlayerInventory, title: Text): HandledScreen<StirlingLiftScreenHandler>(handler, inventory, title) {

    private val texture = Identifier("lifts:textures/gui/stirling_lift.png")

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
    }

    override fun drawForeground(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
        drawCenteredString(matrices, textRenderer, title.string,backgroundWidth/2, 6, 0xFFFFFF)
        textRenderer.draw(matrices, playerInventory.displayName, 8f, backgroundHeight - 96 + 4f, 4210752)
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        client!!.textureManager.bindTexture(texture)
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight)
    }

}