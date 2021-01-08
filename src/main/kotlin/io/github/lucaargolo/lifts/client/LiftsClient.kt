package io.github.lucaargolo.lifts.client

import io.github.lucaargolo.lifts.client.render.entity.EntityRendererCompendium
import io.github.lucaargolo.lifts.network.PacketCompendium
import net.fabricmc.api.ClientModInitializer

class LiftsClient: ClientModInitializer {
    override fun onInitializeClient() {
        PacketCompendium.onInitializeClient()
        EntityRendererCompendium.initialize()
    }
}