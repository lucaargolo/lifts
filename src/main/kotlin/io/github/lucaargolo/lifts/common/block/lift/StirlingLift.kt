package io.github.lucaargolo.lifts.common.block.lift

import io.github.lucaargolo.lifts.common.blockentity.lift.StirlingLiftBlockEntity
import net.minecraft.world.BlockView

class StirlingLift(settings: Settings, platformSpeed: Double, platformRange: Int): Lift(settings, platformSpeed, platformRange) {

    override fun createBlockEntity(world: BlockView?) = StirlingLiftBlockEntity(this)

}