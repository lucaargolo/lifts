package io.github.lucaargolo.lifts.common.block.charger

import io.github.lucaargolo.lifts.common.blockentity.charger.ChargerBlockEntity
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.world.BlockView

class Charger(settings: Settings): BlockWithEntity(settings)  {

    override fun createBlockEntity(world: BlockView?) = ChargerBlockEntity()

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

}