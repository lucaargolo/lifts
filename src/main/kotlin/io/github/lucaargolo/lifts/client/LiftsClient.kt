package io.github.lucaargolo.lifts.client

import io.github.lucaargolo.lifts.client.render.bakedmodel.BakedModelCompendium
import io.github.lucaargolo.lifts.client.render.blockentity.BlockEntityRendererCompendium
import io.github.lucaargolo.lifts.client.render.entity.EntityRendererCompendium
import io.github.lucaargolo.lifts.common.containers.ScreenHandlerCompendium
import io.github.lucaargolo.lifts.network.PacketCompendium
import net.fabricmc.api.ClientModInitializer

class LiftsClient: ClientModInitializer {
    override fun onInitializeClient() {
        PacketCompendium.onInitializeClient()
        ScreenHandlerCompendium.onInitializeClient()
        BlockEntityRendererCompendium.initialize()
        EntityRendererCompendium.initialize()
        BakedModelCompendium.initialize()
    }
}