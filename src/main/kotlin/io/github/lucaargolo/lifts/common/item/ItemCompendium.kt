package io.github.lucaargolo.lifts.common.item

import io.github.lucaargolo.lifts.Lifts.Companion.creativeGroupSettings
import io.github.lucaargolo.lifts.common.block.BlockCompendium
import io.github.lucaargolo.lifts.common.item.wrench.LinkingTool
import io.github.lucaargolo.lifts.utils.RegistryCompendium
import net.minecraft.item.Item
import net.minecraft.util.registry.Registry

object ItemCompendium: RegistryCompendium<Item>(Registry.ITEM) {

    val LINKING_TOOL = register("linking_tool", LinkingTool(creativeGroupSettings().maxCount(1)))

    val RAW_LIFTIUM_INGOT = register("raw_liftium_ingot", LinkingTool(creativeGroupSettings()))
    val LIFTIUM_INGOT = register("liftium_ingot", LinkingTool(creativeGroupSettings()))

    override fun initialize() {
        BlockCompendium.registerBlockItems(map)
        super.initialize()
    }

}