package io.github.lucaargolo.lifts.utils

import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.OrderedText
import net.minecraft.text.Text

object LateTooltipHolder: DrawableHelper() {

    enum class TooltipMode(val f: Int, val s: Int, val t: Int) {
        CLASSIC(0x100010, 0x5000FF, 0x28007F),
        GREEN(0x001009, 0x1AFF00, 0x0D7F00),
        YELLOW(0x091000, 0xFFC300, 0x7F6100),
        RED(0x100D00, 0xFF2500, 0x7F1300)
    }

    private var holdenText: Text? = null
    private var holdenMode: TooltipMode? = null

    fun scheduleLateTooltip(text: Text, mode: TooltipMode) {
        holdenText = text
        holdenMode = mode
    }

    fun onInitializeClient() {
        HudRenderCallback.EVENT.register { matrices, _ ->
            holdenText?.let { renderTooltip(matrices, it, holdenMode ?: TooltipMode.CLASSIC) }
            holdenText = null
        }
    }

    private fun renderTooltip(matrices: MatrixStack, text: Text, tooltipMode: TooltipMode) {
        renderOrderedTooltip(matrices, listOf(text.asOrderedText()), tooltipMode)
    }

    private fun renderOrderedTooltip(matrices: MatrixStack, lines: List<OrderedText>, tooltipMode: TooltipMode) {
        val client = MinecraftClient.getInstance()
        if (lines.isNotEmpty()) {
            val maxLength = lines.map { client.textRenderer.getWidth(it) }.maxOrNull() ?: 0

            val x = client.window.width/(client.options.guiScale*2) - 24
            var y = client.window.height/(client.options.guiScale*2) - 24

            var n = 8
            if (lines.size > 1) {
                n += 2 + (lines.size - 1) * 10
            }

            matrices.push()
            matrices.translate(30.0, 30.0, 0.0)

            val tessellator = Tessellator.getInstance()
            val bufferBuilder = tessellator.buffer
            bufferBuilder.begin(7, VertexFormats.POSITION_COLOR)
            val matrix4f = matrices.peek().model
            val z = 0

            val firstColor: Int = (0xF0000000 + tooltipMode.f).toInt()
            val secondColor: Int = 0x50000000 + tooltipMode.s
            val thirdColor: Int = 0x50000000 + tooltipMode.t

            fillGradient(matrix4f, bufferBuilder, x - 3, y - 4, x + maxLength + 3, y - 3, z, firstColor, firstColor)
            fillGradient(matrix4f, bufferBuilder, x - 3, y + n + 3, x + maxLength + 3, y + n + 4, z, firstColor, firstColor)
            fillGradient(matrix4f, bufferBuilder, x - 3, y - 3, x + maxLength + 3, y + n + 3, z, firstColor, firstColor)
            fillGradient(matrix4f, bufferBuilder, x - 4, y - 3, x - 3, y + n + 3, z, firstColor, firstColor)
            fillGradient(matrix4f, bufferBuilder, x + maxLength + 3, y - 3, x + maxLength + 4, y + n + 3, z, firstColor, firstColor)
            fillGradient(matrix4f, bufferBuilder, x - 3, y - 3 + 1, x - 3 + 1, y + n + 3 - 1, z, secondColor, thirdColor)
            fillGradient(matrix4f, bufferBuilder, x + maxLength + 2, y - 3 + 1, x + maxLength + 3, y + n + 3 - 1, z, secondColor, thirdColor)
            fillGradient(matrix4f, bufferBuilder, x - 3, y - 3, x + maxLength + 3, y - 3 + 1, z, secondColor, secondColor)
            fillGradient(matrix4f, bufferBuilder, x - 3, y + n + 2, x + maxLength + 3, y + n + 3, z, thirdColor, thirdColor)

            RenderSystem.disableTexture()
            bufferBuilder.end()
            BufferRenderer.draw(bufferBuilder)
            RenderSystem.enableTexture()

            lines.forEachIndexed { index, orderedText ->
                client.textRenderer.draw(matrices, orderedText, x.toFloat(), y.toFloat(), -1)
                if (index == 0) {
                    y += 2
                }
                y += 10
            }

            matrices.pop()

        }
    }
    
}