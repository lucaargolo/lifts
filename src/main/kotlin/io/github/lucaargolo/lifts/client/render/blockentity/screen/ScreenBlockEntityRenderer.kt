package io.github.lucaargolo.lifts.client.render.blockentity.screen

import io.github.lucaargolo.lifts.common.blockentity.screen.ScreenBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.BookScreen
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f
import net.minecraft.state.property.Properties
import net.minecraft.util.math.Direction

class ScreenBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher): BlockEntityRenderer<ScreenBlockEntity>(dispatcher) {

    override fun render(entity: ScreenBlockEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider?, light: Int, overlay: Int) {
        val screen = BookScreen()
        screen.init(MinecraftClient.getInstance(), 256, 256)

        matrices.push()
        matrices.translate(0.5, 0.5, 0.5)
        when(entity.cachedState[Properties.HORIZONTAL_FACING]) {
            Direction.SOUTH -> matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180f))
            Direction.EAST -> matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90f))
            Direction.WEST -> matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(270f))
            else -> {}
        }
        matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180f))
        matrices.translate(-0.5, -0.5, -0.5)
        matrices.translate(0.0, 0.0, 12.98/16.0)
        matrices.scale(1/256f, 1/256f, 1/256f)

        screen.render(matrices, 0, 0, 0f)
        matrices.pop()
    }

}