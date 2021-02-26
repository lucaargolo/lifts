package io.github.lucaargolo.lifts.common.block

import io.github.lucaargolo.lifts.Lifts.Companion.creativeGroupSettings
import io.github.lucaargolo.lifts.common.block.lift.ElectricLift
import io.github.lucaargolo.lifts.common.block.lift.StirlingLift
import io.github.lucaargolo.lifts.common.block.screen.ScreenBlock
import io.github.lucaargolo.lifts.mixin.KeyBindingAccessor
import io.github.lucaargolo.lifts.utils.RegistryCompendium
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.util.InputUtil
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import team.reborn.energy.EnergyTier

object BlockCompendium: RegistryCompendium<Block>(Registry.BLOCK) {

    val STIRLING_LIFT = register("stirling_lift", StirlingLift(AbstractBlock.Settings.copy(Blocks.COBBLESTONE), 1.0, 16))

    val ELECTRIC_LIFT_MK1 = register("electric_lift_mk1", ElectricLift(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK), 1.2, 32, 32000.0, EnergyTier.LOW))
    val ELECTRIC_LIFT_MK2 = register("electric_lift_mk2", ElectricLift(AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK), 1.4, 64, 64000.0, EnergyTier.MEDIUM))
    val ELECTRIC_LIFT_MK3 = register("electric_lift_mk3", ElectricLift(AbstractBlock.Settings.copy(Blocks.DIAMOND_BLOCK), 1.6, 128, 128000.0, EnergyTier.HIGH))
    val ELECTRIC_LIFT_MK4 = register("electric_lift_mk4", ElectricLift(AbstractBlock.Settings.copy(Blocks.EMERALD_BLOCK), 1.8, 128, 256000.0, EnergyTier.EXTREME))
    val ELECTRIC_LIFT_MK5 = register("electric_lift_mk5", ElectricLift(AbstractBlock.Settings.copy(Blocks.NETHERITE_BLOCK), 2.0, 256, 512000.0, EnergyTier.INSANE))

    val SCREEN = register("screen", ScreenBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)))

    fun registerBlockItems(itemMap: MutableMap<Identifier, Item>) {
        map.forEach { (identifier, block) ->
            itemMap[identifier] = when(block) {
                is StirlingLift -> object: BlockItem(block, creativeGroupSettings()) {
                    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
                        super.appendTooltip(stack, world, tooltip, context)
                        tooltip.add(TranslatableText("tooltip.powered_by_coal").formatted(Formatting.ITALIC, Formatting.DARK_PURPLE))
                        displayHiddenTooltip(tooltip) {
                            tooltip.add(TranslatableText("tooltip.common.platform_speed").formatted(Formatting.BLUE).append(LiteralText(": ${block.platformSpeed}").formatted(Formatting.GRAY)))
                            tooltip.add(TranslatableText("tooltip.common.platform_range").formatted(Formatting.BLUE).append(LiteralText(": ${block.platformRange}").formatted(Formatting.GRAY)))
                        }
                    }
                }
                is ElectricLift -> object: BlockItem(block, creativeGroupSettings()) {
                    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
                        super.appendTooltip(stack, world, tooltip, context)
                        tooltip.add(TranslatableText("tooltip.powered_by_energy").formatted(Formatting.ITALIC, Formatting.DARK_PURPLE))
                        displayHiddenTooltip(tooltip) {
                            tooltip.add(TranslatableText("tooltip.common.platform_speed").formatted(Formatting.BLUE).append(LiteralText(": ${block.platformSpeed}").formatted(Formatting.GRAY)))
                            tooltip.add(TranslatableText("tooltip.common.platform_range").formatted(Formatting.BLUE).append(LiteralText(": ${block.platformRange}").formatted(Formatting.GRAY)))
                            tooltip.add(TranslatableText("tooltip.common.energy_input").formatted(Formatting.BLUE).append(LiteralText(": ${block.energyTier.maxInput} E/tick").formatted(Formatting.GRAY)))
                            tooltip.add(TranslatableText("tooltip.common.energy_capacity").formatted(Formatting.BLUE).append(LiteralText(": ${block.energyCapacity} E").formatted(Formatting.GRAY)))
                        }
                    }
                }
                else -> BlockItem(block, creativeGroupSettings())
            }
        }
    }

    fun displayHiddenTooltip(tooltip: MutableList<Text>, runnable: Runnable) {
        val client = MinecraftClient.getInstance()
        val sneakKey = client.options.keySneak
        val sneak = InputUtil.isKeyPressed(client.window.handle, (sneakKey as KeyBindingAccessor).boundKey.code)
        if (!sneak) {
            val text = TranslatableText("tooltip.sneak_for_more")
            val parts = text.string.split("<KEY>")
            if (parts.size == 2) {
                tooltip.add(LiteralText(parts[0]).formatted(Formatting.BLUE).append(TranslatableText(sneakKey.boundKeyTranslationKey).formatted(Formatting.GRAY)).append(LiteralText(parts[1]).formatted(Formatting.BLUE)))
            } else {
                tooltip.add(TranslatableText("tooltip.malformed_string").formatted(Formatting.DARK_RED))
            }
        }else{
            runnable.run()
        }
    }

}