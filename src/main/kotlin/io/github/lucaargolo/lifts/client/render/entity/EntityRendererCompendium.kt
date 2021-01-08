package io.github.lucaargolo.lifts.client.render.entity

import io.github.lucaargolo.lifts.client.render.entity.platform.PlatformEntityRenderer
import io.github.lucaargolo.lifts.common.entity.EntityCompendium
import io.github.lucaargolo.lifts.utils.GenericCompendium
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry

object EntityRendererCompendium: GenericCompendium<EntityRendererRegistry.Factory>() {

    val PLATFORM_FACTORY = register("platform") { dispatcher, _ ->
        PlatformEntityRenderer(dispatcher)
    }

    override fun initialize() {
        map.forEach { (entityIdentifier, entityRendererRegistryFactory) ->
            EntityRendererRegistry.INSTANCE.register(EntityCompendium.get(entityIdentifier), entityRendererRegistryFactory)
        }
    }
}