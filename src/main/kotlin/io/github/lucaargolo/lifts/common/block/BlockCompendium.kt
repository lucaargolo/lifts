package io.github.lucaargolo.lifts.common.block

import io.github.lucaargolo.lifts.Lifts
import io.github.lucaargolo.lifts.Lifts.Companion.creativeGroupSettings
import io.github.lucaargolo.lifts.common.block.charger.Charger
import io.github.lucaargolo.lifts.common.block.lift.ElectricLift
import io.github.lucaargolo.lifts.common.block.lift.StirlingLift
import io.github.lucaargolo.lifts.common.block.misc.LiftButton
import io.github.lucaargolo.lifts.common.block.misc.LiftDetector
import io.github.lucaargolo.lifts.common.block.screen.ScreenBlock
import io.github.lucaargolo.lifts.mixin.KeyBindingAccessor
import io.github.lucaargolo.lifts.utils.RegistryCompendium
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.SlabBlock
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

object BlockCompendium: RegistryCompendium<Block>(Registry.BLOCK) {

    val COAL_STRUCTURE = register("coal_structure", Block(AbstractBlock.Settings.copy(Blocks.COAL_BLOCK).nonOpaque()))
    val IRON_STRUCTURE = register("iron_structure", Block(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).nonOpaque()))
    val GOLD_STRUCTURE = register("gold_structure", Block(AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).nonOpaque()))
    val DIAMOND_STRUCTURE = register("diamond_structure", Block(AbstractBlock.Settings.copy(Blocks.DIAMOND_BLOCK).nonOpaque()))
    val EMERALD_STRUCTURE = register("emerald_structure", Block(AbstractBlock.Settings.copy(Blocks.EMERALD_BLOCK).nonOpaque()))
    val NETHERITE_STRUCTURE = register("netherite_structure", Block(AbstractBlock.Settings.copy(Blocks.NETHERITE_BLOCK).nonOpaque()))

    val MACHINE_BLOCK = register("machine_block", Block(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)))
    val MACHINE_BLOCK_SLAB = register("machine_block_slab", SlabBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)))
    val STIRLING_MACHINE_BLOCK = register("stirling_machine_block", Block(AbstractBlock.Settings.copy(Blocks.COBBLESTONE)))

    val STIRLING_LIFT = register("stirling_lift", StirlingLift(AbstractBlock.Settings.copy(Blocks.COBBLESTONE), Lifts.CONFIG.liftConfigs.stirlingLift))
    val ELECTRIC_LIFT_MK1 = register("electric_lift_mk1", ElectricLift(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK), Lifts.CONFIG.liftConfigs.electricLiftMk1, 32))
    val ELECTRIC_LIFT_MK2 = register("electric_lift_mk2", ElectricLift(AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK), Lifts.CONFIG.liftConfigs.electricLiftMk2, 128))
    val ELECTRIC_LIFT_MK3 = register("electric_lift_mk3", ElectricLift(AbstractBlock.Settings.copy(Blocks.DIAMOND_BLOCK), Lifts.CONFIG.liftConfigs.electricLiftMk3, 512))
    val ELECTRIC_LIFT_MK4 = register("electric_lift_mk4", ElectricLift(AbstractBlock.Settings.copy(Blocks.EMERALD_BLOCK), Lifts.CONFIG.liftConfigs.electricLiftMk4, 2048))
    val ELECTRIC_LIFT_MK5 = register("electric_lift_mk5", ElectricLift(AbstractBlock.Settings.copy(Blocks.NETHERITE_BLOCK), Lifts.CONFIG.liftConfigs.electricLiftMk5, 8192))

    val LIFT_DETECTOR = register("lift_detector", LiftDetector(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)))

    val LIFT_BUTTON = register("lift_button", LiftButton(AbstractBlock.Settings.copy(Blocks.STONE_BUTTON)))

    val SCREEN_CHARGER = register("screen_charger", Charger(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)))

    val SCREEN = register("screen", ScreenBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)))

    fun registerBlockItems(itemMap: MutableMap<Identifier, Item>) {
        map.forEach { (identifier, block) ->
            val itemSettings = creativeGroupSettings().let {
                if(block == NETHERITE_STRUCTURE || block == ELECTRIC_LIFT_MK5) { it.fireproof() } else { it }
            }
            itemMap[identifier] = when(block) {
                is StirlingLift -> object: BlockItem(block, itemSettings) {
                    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
                        super.appendTooltip(stack, world, tooltip, context)
                        tooltip.add(TranslatableText("tooltip.lifts.powered_by_coal").formatted(Formatting.ITALIC, Formatting.DARK_PURPLE))
                        world?.let {
                            displayHiddenTooltip(tooltip) {
                                tooltip.add(TranslatableText("tooltip.lifts.common.platform_speed").formatted(Formatting.BLUE).append(LiteralText(": ${block.liftConfig.platformSpeed}").formatted(Formatting.GRAY)))
                                tooltip.add(TranslatableText("tooltip.lifts.common.platform_range").formatted(Formatting.BLUE).append(LiteralText(": ${block.liftConfig.platformRange} ").append(TranslatableText("tooltip.lifts.common.blocks")).formatted(Formatting.GRAY)))
                            }
                        }
                    }
                }
                is ElectricLift -> object: BlockItem(block, itemSettings) {
                    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
                        super.appendTooltip(stack, world, tooltip, context)
                        tooltip.add(TranslatableText("tooltip.lifts.powered_by_energy").formatted(Formatting.ITALIC, Formatting.DARK_PURPLE))
                        world?.let {
                            displayHiddenTooltip(tooltip) {
                                tooltip.add(TranslatableText("tooltip.lifts.common.platform_speed").formatted(Formatting.BLUE).append(LiteralText(": ${block.liftConfig.platformSpeed}").formatted(Formatting.GRAY)))
                                tooltip.add(TranslatableText("tooltip.lifts.common.platform_range").formatted(Formatting.BLUE).append(LiteralText(": ${block.liftConfig.platformRange} ").append(TranslatableText("tooltip.lifts.common.blocks")).formatted(Formatting.GRAY)))
                                tooltip.add(TranslatableText("tooltip.lifts.common.energy_input").formatted(Formatting.BLUE).append(LiteralText(": ${block.liftMaxExtract} E/tick").formatted(Formatting.GRAY)))
                                tooltip.add(TranslatableText("tooltip.lifts.common.energy_capacity").formatted(Formatting.BLUE).append(LiteralText(": ${block.electricLiftConfig.energyCapacity} E").formatted(Formatting.GRAY)))
                            }
                        }
                    }
                }
                is LiftDetector, is LiftButton, is Charger, is ScreenBlock -> object: BlockItem(block, itemSettings) {
                    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
                        super.appendTooltip(stack, world, tooltip, context)
                        tooltip.add(TranslatableText("tooltip.lifts.${identifier.path}").formatted(Formatting.ITALIC, Formatting.DARK_PURPLE))
                    }
                }
                else -> BlockItem(block, itemSettings)
            }
        }
    }

    fun displayHiddenTooltip(tooltip: MutableList<Text>, runnable: Runnable) {
        val client = MinecraftClient.getInstance()
        val sneakKey = client.options.sneakKey
        val sneak = InputUtil.isKeyPressed(client.window.handle, (sneakKey as KeyBindingAccessor).boundKey.code)
        if (!sneak) {
            val text = TranslatableText("tooltip.lifts.sneak_for_more")
            val parts = text.string.split("<KEY>")
            if (parts.size == 2) {
                tooltip.add(LiteralText(parts[0]).formatted(Formatting.BLUE).append(TranslatableText(sneakKey.boundKeyTranslationKey).formatted(Formatting.GRAY)).append(LiteralText(parts[1]).formatted(Formatting.BLUE)))
            } else {
                tooltip.add(TranslatableText("tooltip.lifts.malformed_string").formatted(Formatting.DARK_RED))
            }
        }else{
            runnable.run()
        }
    }

}