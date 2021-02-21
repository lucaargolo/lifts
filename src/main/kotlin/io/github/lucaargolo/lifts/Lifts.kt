package io.github.lucaargolo.lifts

import io.github.lucaargolo.lifts.common.block.BlockCompendium
import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.common.entity.EntityCompendium
import io.github.lucaargolo.lifts.common.item.ItemCompendium
import io.github.lucaargolo.lifts.network.PacketCompendium
import io.github.lucaargolo.lifts.utils.ModIdentifier
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

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

        private val creativeTab = FabricItemGroupBuilder.create(ModIdentifier("creative_tab")).icon{ ItemStack(BlockCompendium.LIFT) }.build()
        fun creativeGroupSettings(): Item.Settings = Item.Settings().group(creativeTab)
    }

}