package io.github.lucaargolo.lifts.client.render.entity

import io.github.lucaargolo.lifts.client.render.entity.platform.PlatformEntityRenderer
import io.github.lucaargolo.lifts.common.entity.EntityCompendium
import io.github.lucaargolo.lifts.utils.GenericCompendium
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import java.util.function.Function

object EntityRendererCompendium: GenericCompendium<Function<EntityRendererFactory.Context, EntityRenderer<*>>>() {

    val PLATFORM_FACTORY = register("platform") { PlatformEntityRenderer(it) }

    @Suppress("UNCHECKED_CAST")
    override fun initialize() {
        map.forEach { (entityIdentifier, entityRendererRegistryFactory) ->
            EntityRendererRegistry.INSTANCE.register(EntityCompendium.get(entityIdentifier) as EntityType<Entity>) { context -> entityRendererRegistryFactory.apply(context) as EntityRenderer<Entity>}
        }
    }
}