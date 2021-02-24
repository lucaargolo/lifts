package io.github.lucaargolo.lifts.common.block

import io.github.lucaargolo.lifts.Lifts.Companion.creativeGroupSettings
import io.github.lucaargolo.lifts.common.block.lift.ElectricLift
import io.github.lucaargolo.lifts.common.block.lift.Lift
import io.github.lucaargolo.lifts.common.block.lift.StirlingLift
import io.github.lucaargolo.lifts.common.block.screen.ScreenBlock
import io.github.lucaargolo.lifts.utils.RegistryCompendium
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import team.reborn.energy.EnergyTier

object BlockCompendium: RegistryCompendium<Block>(Registry.BLOCK) {

    val STIRLING_LIFT = register("stirling_lift", StirlingLift(AbstractBlock.Settings.copy(Blocks.FURNACE), 1.0, 16))

    val ELECTRIC_LIFT_MK1 = register("electric_lift_mk1", ElectricLift(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK), 1.2, 32, 32000.0, EnergyTier.LOW))
    val ELECTRIC_LIFT_MK2 = register("electric_lift_mk2", ElectricLift(AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK), 1.4, 64, 64000.0, EnergyTier.MEDIUM))
    val ELECTRIC_LIFT_MK3 = register("electric_lift_mk3", ElectricLift(AbstractBlock.Settings.copy(Blocks.DIAMOND_BLOCK), 1.6, 128, 128000.0, EnergyTier.HIGH))
    val ELECTRIC_LIFT_MK4 = register("electric_lift_mk4", ElectricLift(AbstractBlock.Settings.copy(Blocks.EMERALD_BLOCK), 1.8, 128, 256000.0, EnergyTier.EXTREME))
    val ELECTRIC_LIFT_MK5 = register("electric_lift_mk5", ElectricLift(AbstractBlock.Settings.copy(Blocks.NETHERITE_BLOCK), 2.0, 256, 512000.0, EnergyTier.INSANE))

    val SCREEN = register("screen", ScreenBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)))

    fun registerBlockItems(itemMap: MutableMap<Identifier, Item>) {
        map.forEach { (identifier, block) ->
            itemMap[identifier] = BlockItem(block, creativeGroupSettings())
        }
    }

}