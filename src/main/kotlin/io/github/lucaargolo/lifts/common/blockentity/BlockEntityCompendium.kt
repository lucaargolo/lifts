package io.github.lucaargolo.lifts.common.blockentity

import io.github.lucaargolo.lifts.common.block.BlockCompendium
import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import io.github.lucaargolo.lifts.utils.RegistryCompendium
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.registry.Registry

@Suppress("UNCHECKED_CAST")
object BlockEntityCompendium: RegistryCompendium<BlockEntityType<*>>(Registry.BLOCK_ENTITY_TYPE) {

    val LIFT_TYPE = register("lift", BlockEntityType.Builder.create( { LiftBlockEntity(null) }, BlockCompendium.LIFT ).build(null)) as BlockEntityType<LiftBlockEntity>

}