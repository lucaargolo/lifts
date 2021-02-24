package io.github.lucaargolo.lifts.common.blockentity

import io.github.lucaargolo.lifts.common.block.BlockCompendium
import io.github.lucaargolo.lifts.common.blockentity.lift.ElectricLiftBlockEntity
import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import io.github.lucaargolo.lifts.common.blockentity.lift.StirlingLiftBlockEntity
import io.github.lucaargolo.lifts.common.blockentity.screen.ScreenBlockEntity
import io.github.lucaargolo.lifts.utils.RegistryCompendium
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.registry.Registry
import team.reborn.energy.EnergyTier

@Suppress("UNCHECKED_CAST")
object BlockEntityCompendium: RegistryCompendium<BlockEntityType<*>>(Registry.BLOCK_ENTITY_TYPE) {

    val STIRLING_LIFT_TYPE = register("lift", BlockEntityType.Builder.create( { StirlingLiftBlockEntity(null) }, BlockCompendium.STIRLING_LIFT ).build(null)) as BlockEntityType<LiftBlockEntity>
    val ELECTRIC_LIFT_TYPE = register("lift", BlockEntityType.Builder.create( { ElectricLiftBlockEntity(null, 0.0, EnergyTier.INFINITE) }, BlockCompendium.ELECTRIC_LIFT_MK1, BlockCompendium.ELECTRIC_LIFT_MK2, BlockCompendium.ELECTRIC_LIFT_MK3, BlockCompendium.ELECTRIC_LIFT_MK4, BlockCompendium.ELECTRIC_LIFT_MK5 ).build(null)) as BlockEntityType<LiftBlockEntity>

    val SCREEN_TYPE = register("screen", BlockEntityType.Builder.create( { ScreenBlockEntity() }, BlockCompendium.SCREEN ).build(null)) as BlockEntityType<ScreenBlockEntity>


}