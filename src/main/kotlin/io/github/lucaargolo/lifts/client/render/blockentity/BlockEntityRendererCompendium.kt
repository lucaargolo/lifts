package io.github.lucaargolo.lifts.client.render.blockentity

import io.github.lucaargolo.lifts.client.render.blockentity.screen.ScreenBlockEntityRenderer
import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.utils.GenericCompendium
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory

object BlockEntityRendererCompendium: GenericCompendium<BlockEntityRendererFactory<*>>() {

    val SCREEN_FACTORY = register("screen", BlockEntityRendererFactory { ScreenBlockEntityRenderer(it.renderDispatcher) })

    @Suppress("UNCHECKED_CAST")
    override fun initialize() {
        map.forEach { (entityIdentifier, renderFactory) ->
            BlockEntityRendererRegistry.register(BlockEntityCompendium.get(entityIdentifier) as BlockEntityType<BlockEntity>, renderFactory as BlockEntityRendererFactory<BlockEntity>)
        }
    }

}