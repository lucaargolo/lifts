package io.github.lucaargolo.lifts.client.screen

import io.github.lucaargolo.lifts.Lifts
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.TranslatableText

class NoEnergyScreen: Screen(TranslatableText("screen.lifts.title.no_energy")) {

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        matrices.push()
        matrices.scale(Lifts.CONFIG.screenScale+0f, Lifts.CONFIG.screenScale+0f, Lifts.CONFIG.screenScale+0f)
        drawCenteredText(matrices, textRenderer, TranslatableText("screen.lifts.title.no_energy"), 64, 10, 0xFF0000)
        super.render(matrices, mouseX, mouseY, delta)
        matrices.pop()
    }


}