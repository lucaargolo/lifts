package io.github.lucaargolo.lifts.client.screen.screen

import io.github.lucaargolo.lifts.common.screenhandlers.screen.ScreenScreenHandler
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.TranslatableText

class ScreenScreen(handler: ScreenScreenHandler, playerInventory: PlayerInventory): HandledScreen<ScreenScreenHandler>(handler, playerInventory, TranslatableText("screen.screen")) {



    override fun drawBackground(matrices: MatrixStack?, delta: Float, mouseX: Int, mouseY: Int) {
        //TODO("Not yet implemented")
    }
}