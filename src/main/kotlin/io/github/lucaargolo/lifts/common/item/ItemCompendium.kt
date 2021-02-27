package io.github.lucaargolo.lifts.common.item

import io.github.lucaargolo.lifts.Lifts.Companion.creativeGroupSettings
import io.github.lucaargolo.lifts.common.block.BlockCompendium
import io.github.lucaargolo.lifts.common.item.linking.LinkingTool
import io.github.lucaargolo.lifts.utils.RegistryCompendium
import net.minecraft.item.Item
import net.minecraft.util.registry.Registry

object ItemCompendium: RegistryCompendium<Item>(Registry.ITEM) {

    val LINKING_TOOL = register("linking_tool", LinkingTool(creativeGroupSettings().maxCount(1)))

    override fun initialize() {
        BlockCompendium.registerBlockItems(map)
        super.initialize()
    }

}