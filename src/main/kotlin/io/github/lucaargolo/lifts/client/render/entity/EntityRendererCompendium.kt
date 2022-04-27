package io.github.lucaargolo.lifts.client.render.entity

import io.github.lucaargolo.lifts.client.render.entity.platform.PlatformEntityRenderer
import io.github.lucaargolo.lifts.common.entity.EntityCompendium
import io.github.lucaargolo.lifts.utils.GenericCompendium
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType

object EntityRendererCompendium: GenericCompendium<EntityRendererFactory<*>>() {

    val PLATFORM_FACTORY = register("platform", EntityRendererFactory { PlatformEntityRenderer(it) })

    @Suppress("UNCHECKED_CAST")
    override fun initialize() {
        map.forEach { (entityIdentifier, entityRendererFactory) ->
            EntityRendererRegistry.register(EntityCompendium.get(entityIdentifier) as EntityType<Entity>, entityRendererFactory as EntityRendererFactory<Entity>)
        }
    }
}