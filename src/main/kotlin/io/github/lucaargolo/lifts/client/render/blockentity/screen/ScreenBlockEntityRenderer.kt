package io.github.lucaargolo.lifts.client.render.blockentity.screen

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.lifts.common.block.screen.ScreenBlockHandler
import io.github.lucaargolo.lifts.common.blockentity.screen.ScreenBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.TitleScreen
import net.minecraft.client.render.*
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f
import net.minecraft.state.property.Properties
import net.minecraft.util.math.Direction

class ScreenBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher): BlockEntityRenderer<ScreenBlockEntity>(dispatcher) {

    override fun render(entity: ScreenBlockEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val framebuffer = ScreenBlockHandler.screenFramebuffer ?: return

        if(!entity.isScreenSetup) {
            entity.setupScreen(TitleScreen())
        }

        val facing = entity.cachedState[Properties.HORIZONTAL_FACING]
        val hitResult = dispatcher.crosshairTarget
        val mousePos = ScreenBlockHandler.getMousePosition(hitResult, facing, entity.pos)

        framebuffer.beginWrite(true)

        RenderSystem.clear(256, MinecraftClient.IS_SYSTEM_MAC)
        RenderSystem.matrixMode(5889)
        RenderSystem.pushMatrix()
        RenderSystem.loadIdentity()
        RenderSystem.ortho(0.0, framebuffer.textureWidth.toDouble(), framebuffer.textureHeight.toDouble(), 0.0, 1000.0, 3000.0)
        RenderSystem.matrixMode(5888)
        RenderSystem.pushMatrix()
        RenderSystem.loadIdentity()
        RenderSystem.translatef(0.0f, 0.0f, -2000.0f)
        RenderSystem.fogMode(GlStateManager.FogMode.EXP2)
        entity.screen?.render(MatrixStack(), mousePos.first.toInt(), mousePos.second.toInt(), MinecraftClient.getInstance().lastFrameDuration)
        RenderSystem.fogMode(GlStateManager.FogMode.LINEAR)
        RenderSystem.matrixMode(5888)
        RenderSystem.popMatrix()
        RenderSystem.matrixMode(5889)
        RenderSystem.popMatrix()

        RenderSystem.enableDepthTest()

        framebuffer.endWrite()

        MinecraftClient.getInstance().framebuffer.beginWrite(true)

        matrices.push()
        matrices.translate(0.5, 0.5, 0.5)
        when(facing) {
            Direction.SOUTH -> matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180f))
            Direction.EAST -> matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(270f))
            Direction.WEST -> matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90f))
            else -> {}
        }
        matrices.translate(-0.5, -0.5, -0.5)
        matrices.translate(0.0, 0.0, 12.99/16.0)
        matrices.scale(1f/framebuffer.textureWidth, 1f/framebuffer.textureHeight, 1f)

        framebuffer.beginRead()


        val bufferBuilder = Tessellator.getInstance().buffer
        val matrix = matrices.peek().model

        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE)
        bufferBuilder.vertex(matrix, 0f, framebuffer.textureHeight.toFloat(), 0f).texture(1f, 1f).next()
        bufferBuilder.vertex(matrix, framebuffer.textureWidth.toFloat(), framebuffer.textureHeight.toFloat(), 0f).texture(0f, 1f).next()
        bufferBuilder.vertex(matrix, framebuffer.textureWidth.toFloat(), 0f, 0f).texture(0f, 0f).next()
        bufferBuilder.vertex(matrix, 0f, 0f, 0f).texture(1f, 0f).next()
        bufferBuilder.end()
        RenderSystem.enableAlphaTest()
        BufferRenderer.draw(bufferBuilder)

        framebuffer.endRead()
        matrices.pop()

    }

}