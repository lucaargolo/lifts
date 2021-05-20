package io.github.lucaargolo.lifts.client.render.entity.platform

import io.github.lucaargolo.lifts.common.entity.platform.PlatformEntity
import net.minecraft.block.BlockRenderType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.util.*

class PlatformEntityRenderer(context: EntityRendererFactory.Context): EntityRenderer<PlatformEntity>(context) {

    override fun render(entity: PlatformEntity, yaw: Float, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int) {
        entity.blockMatrix?.forEachIndexed { x, matrix ->
            matrix?.forEachIndexed { z, blockState ->
                if (blockState!!.renderType == BlockRenderType.MODEL) {
                    val world = entity.world
                    matrices.push()
                    val blockPos = BlockPos(entity.x, entity.boundingBox.maxY, entity.z)
                    matrices.translate(x-0.5, 0.0, z-0.5)
                    val blockRenderManager = MinecraftClient.getInstance().blockRenderManager
                    blockRenderManager.modelRenderer.renderFlat(world, blockRenderManager.getModel(blockState), blockState, blockPos, matrices, vertexConsumers.getBuffer(RenderLayers.getMovingBlockLayer(blockState)), false, Random(), blockState.getRenderingSeed(blockPos), OverlayTexture.DEFAULT_UV)
                    matrices.pop()
                }
            }
        }
    }

    override fun getTexture(entity: PlatformEntity?): Identifier {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE
    }

}