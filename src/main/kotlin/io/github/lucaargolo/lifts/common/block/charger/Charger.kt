package io.github.lucaargolo.lifts.common.block.charger

import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.common.blockentity.charger.ChargerBlockEntity
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class Charger(settings: Settings): BlockWithEntity(settings)  {

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = ChargerBlockEntity(pos, state)

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

    override fun <T : BlockEntity?> getTicker(world: World?, state: BlockState?, type: BlockEntityType<T>?): BlockEntityTicker<T>? {
        return checkType(type, BlockEntityCompendium.SCREEN_CHARGER_TYPE, ChargerBlockEntity::commonTick)
    }

}