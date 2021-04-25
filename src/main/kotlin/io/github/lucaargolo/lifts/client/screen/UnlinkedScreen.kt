package io.github.lucaargolo.lifts.client.screen

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

class UnlinkedScreen: Screen(TranslatableText("screen.lifts.title.unlinked")) {

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        drawCenteredText(matrices, textRenderer, TranslatableText("screen.lifts.title.unlinked"), 64, 10, 0xFFFF00)

        val linkingToolString = TranslatableText("screen.lifts.please_use_linking_tool").string
        val splitStringArray = linkingToolString.split(Regex("<KEY\\d>"))

        val finalText = if(splitStringArray.size == 4) {
            val client = MinecraftClient.getInstance()
            val sneakKey = client.options.keySneak
            val useKey = client.options.keyUse
            LiteralText(splitStringArray[0]).formatted(Formatting.BLUE)
                .append(TranslatableText(sneakKey.boundKeyTranslationKey).formatted(Formatting.GRAY))
                .append(LiteralText(splitStringArray[1]).formatted(Formatting.BLUE))
                .append(TranslatableText(useKey.boundKeyTranslationKey).formatted(Formatting.GRAY))
                .append(LiteralText(splitStringArray[2]).formatted(Formatting.BLUE))
                .append(TranslatableText(useKey.boundKeyTranslationKey).formatted(Formatting.GRAY))
                .append(LiteralText(splitStringArray[3]).formatted(Formatting.BLUE))
        }else{
            TranslatableText("tooltip.lifts.malformed_string").formatted(Formatting.DARK_RED)
        }

        textRenderer.wrapLines(finalText, 108).forEachIndexed { index, text ->
            textRenderer.draw(matrices, text, (64f - textRenderer.getWidth(text)/2), 20f+(index*10), 0xFFFFFF)
        }
        super.render(matrices, mouseX, mouseY, delta)
    }


}