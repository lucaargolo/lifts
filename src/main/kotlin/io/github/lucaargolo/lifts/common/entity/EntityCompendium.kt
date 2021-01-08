package io.github.lucaargolo.lifts.common.entity

import io.github.lucaargolo.lifts.common.entity.platform.PlatformEntity
import io.github.lucaargolo.lifts.utils.RegistryCompendium
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

object EntityCompendium: RegistryCompendium<EntityType<*>>(Registry.ENTITY_TYPE) {

    val PLATFORM_TYPE = register ("platform",
            FabricEntityTypeBuilder.create(SpawnGroup.MISC) { type: EntityType<PlatformEntity>, world: World -> PlatformEntity(type, world) }.dimensions(EntityDimensions.changing(0.25f, 0.25f)).build()
    )

}