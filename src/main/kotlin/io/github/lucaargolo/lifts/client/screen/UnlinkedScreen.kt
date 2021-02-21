package io.github.lucaargolo.lifts.client.screen

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText

class UnlinkedScreen: Screen(LiteralText("Unlinked")) {

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        drawCenteredText(matrices, textRenderer, LiteralText("Unlinked Screen"), 64, 10, 0xFF0000)
        super.render(matrices, mouseX, mouseY, delta)
    }


}