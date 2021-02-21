package io.github.lucaargolo.lifts.common.block

import io.github.lucaargolo.lifts.Lifts.Companion.creativeGroupSettings
import io.github.lucaargolo.lifts.common.block.lift.Lift
import io.github.lucaargolo.lifts.common.block.screen.ScreenBlock
import io.github.lucaargolo.lifts.utils.RegistryCompendium
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object BlockCompendium: RegistryCompendium<Block>(Registry.BLOCK) {

    val LIFT = register("lift", Lift(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)))
    val SCREEN = register("screen", ScreenBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)))

    fun registerBlockItems(itemMap: MutableMap<Identifier, Item>) {
        map.forEach { (identifier, block) ->
            itemMap[identifier] = BlockItem(block, creativeGroupSettings())
        }
    }

}