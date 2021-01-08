package io.github.lucaargolo.lifts

import io.github.lucaargolo.lifts.common.block.BlockCompendium
import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.common.entity.EntityCompendium
import io.github.lucaargolo.lifts.common.item.ItemCompendium
import io.github.lucaargolo.lifts.network.PacketCompendium
import net.fabricmc.api.ModInitializer

class Lifts: ModInitializer {

    override fun onInitialize() {
        PacketCompendium.onInitialize()
        BlockCompendium.initialize()
        BlockEntityCompendium.initialize()
        ItemCompendium.initialize()
        EntityCompendium.initialize()
    }

    companion object {
        const val MOD_ID = "lifts"
    }

}