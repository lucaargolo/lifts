package io.github.lucaargolo.lifts.client.render.blockentity.screen

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.lifts.client.screen.FloorSelectionScreen
import io.github.lucaargolo.lifts.client.screen.NoEnergyScreen
import io.github.lucaargolo.lifts.client.screen.UnlinkedScreen
import io.github.lucaargolo.lifts.common.block.screen.ScreenBlockHandler
import io.github.lucaargolo.lifts.common.blockentity.screen.ScreenBlockEntity
import io.github.lucaargolo.lifts.compat.OptifineShadersCompat
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.state.property.Properties
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Matrix4f
import net.minecraft.util.math.Vec3f

class ScreenBlockEntityRenderer(private val dispatcher: BlockEntityRenderDispatcher): BlockEntityRenderer<ScreenBlockEntity> {

    @Suppress("DEPRECATION")
    override fun render(entity: ScreenBlockEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val framebuffer = ScreenBlockHandler.screenFramebuffer ?: return
        val client = MinecraftClient.getInstance()

        when(entity.screen) {
            null -> {
                when(entity.state) {
                    ScreenBlockEntity.State.NO_ENERGY -> NoEnergyScreen()
                    ScreenBlockEntity.State.UNLINKED -> UnlinkedScreen()
                    ScreenBlockEntity.State.LINKED -> entity.linkedLift?.let { FloorSelectionScreen(it) }
                }?.let { screen ->
                    screen.init(client, framebuffer.textureWidth, framebuffer.textureHeight)
                    entity.screen = screen
                }
            }
        }

        val facing = entity.cachedState[Properties.HORIZONTAL_FACING]
        val hitResult = dispatcher.crosshairTarget
        val mousePos = ScreenBlockHandler.getMousePosition(hitResult, facing, entity.pos)

        OptifineShadersCompat.startDrawingScreen()
        framebuffer.beginWrite(true)

        RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 1.0f)
        RenderSystem.clear(16384, MinecraftClient.IS_SYSTEM_MAC)
        RenderSystem.backupProjectionMatrix()
        RenderSystem.setProjectionMatrix(Matrix4f.projectionMatrix(0.0f, framebuffer.textureWidth.toFloat(), 0.0f, framebuffer.textureHeight.toFloat(), 1000.0f, 3000.0f))
        val modelViewStack = RenderSystem.getModelViewStack()
        modelViewStack.push()
        modelViewStack.loadIdentity()
        modelViewStack.translate(0.0, 0.0, -2000.0)
        RenderSystem.applyModelViewMatrix()
        RenderSystem.setShaderFogColor(0.0f, 0.0f, 0.0f, 0.0f)
        entity.screen?.render(MatrixStack(), mousePos.first.toInt(), mousePos.second.toInt(), client.lastFrameDuration)
        BackgroundRenderer.setFogBlack()
        modelViewStack.pop()
        RenderSystem.applyModelViewMatrix()
        RenderSystem.restoreProjectionMatrix()

        framebuffer.endWrite()
        MinecraftClient.getInstance().framebuffer.beginWrite(true)
        OptifineShadersCompat.endDrawingScreen()
        RenderSystem.enableDepthTest()

        matrices.push()
        matrices.translate(0.5, 0.5, 0.5)
        when(facing) {
            Direction.SOUTH -> matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180f))
            Direction.EAST -> matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(270f))
            Direction.WEST -> matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90f))
            else -> {}
        }
        matrices.translate(-0.5, -0.5, -0.5)
        matrices.translate(0.0, 0.0, 12.99/16.0)
        matrices.scale(1f/framebuffer.textureWidth, 1f/framebuffer.textureHeight, 1f)

        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        RenderSystem.setShaderTexture(0, framebuffer.colorAttachment)
        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.buffer
        val matrix = matrices.peek().model

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
        bufferBuilder.vertex(matrix, 0f, framebuffer.textureHeight.toFloat(), 0f).texture(1f, 1f).next()
        bufferBuilder.vertex(matrix, framebuffer.textureWidth.toFloat(), framebuffer.textureHeight.toFloat(), 0f).texture(0f, 1f).next()
        bufferBuilder.vertex(matrix, framebuffer.textureWidth.toFloat(), 0f, 0f).texture(0f, 0f).next()
        bufferBuilder.vertex(matrix, 0f, 0f, 0f).texture(1f, 0f).next()
        tessellator.draw()

        matrices.pop()

    }

}