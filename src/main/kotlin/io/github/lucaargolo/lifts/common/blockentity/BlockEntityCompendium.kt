package io.github.lucaargolo.lifts.common.blockentity

import io.github.lucaargolo.lifts.common.block.BlockCompendium
import io.github.lucaargolo.lifts.common.blockentity.charger.ChargerBlockEntity
import io.github.lucaargolo.lifts.common.blockentity.lift.ElectricLiftBlockEntity
import io.github.lucaargolo.lifts.common.blockentity.lift.StirlingLiftBlockEntity
import io.github.lucaargolo.lifts.common.blockentity.misc.LiftButtonBlockEntity
import io.github.lucaargolo.lifts.common.blockentity.misc.LiftDetectorBlockEntity
import io.github.lucaargolo.lifts.common.blockentity.screen.ScreenBlockEntity
import io.github.lucaargolo.lifts.utils.RegistryCompendium
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.registry.Registry

@Suppress("UNCHECKED_CAST")
object BlockEntityCompendium: RegistryCompendium<BlockEntityType<*>>(Registry.BLOCK_ENTITY_TYPE) {

    val STIRLING_LIFT_TYPE = register("stirling_lift", BlockEntityType.Builder.create( { pos, state -> StirlingLiftBlockEntity(pos, state) }, BlockCompendium.STIRLING_LIFT ).build(null)) as BlockEntityType<StirlingLiftBlockEntity>
    val ELECTRIC_LIFT_TYPE = register("electric_lift", BlockEntityType.Builder.create( { pos, state -> ElectricLiftBlockEntity(pos, state) }, BlockCompendium.ELECTRIC_LIFT_MK1, BlockCompendium.ELECTRIC_LIFT_MK2, BlockCompendium.ELECTRIC_LIFT_MK3, BlockCompendium.ELECTRIC_LIFT_MK4, BlockCompendium.ELECTRIC_LIFT_MK5 ).build(null)) as BlockEntityType<ElectricLiftBlockEntity>

    val LIFT_DETECTOR_TYPE = register("lift_detector", BlockEntityType.Builder.create( { pos, state -> LiftDetectorBlockEntity(pos, state)}, BlockCompendium.LIFT_DETECTOR).build(null)) as BlockEntityType<LiftDetectorBlockEntity>

    val LIFT_BUTTON_TYPE = register("lift_button", BlockEntityType.Builder.create( { pos, state -> LiftButtonBlockEntity(pos, state) }, BlockCompendium.LIFT_BUTTON).build(null)) as BlockEntityType<LiftButtonBlockEntity>

    val SCREEN_CHARGER_TYPE = register("screen_charger", BlockEntityType.Builder.create( { pos, state -> ChargerBlockEntity(pos, state) }, BlockCompendium.SCREEN_CHARGER).build(null)) as BlockEntityType<ChargerBlockEntity>

    val SCREEN_TYPE = register("screen", BlockEntityType.Builder.create( { pos, state -> ScreenBlockEntity(pos, state) }, BlockCompendium.SCREEN ).build(null)) as BlockEntityType<ScreenBlockEntity>

}