package io.github.lucaargolo.lifts.common.blockentity.lift

import net.minecraft.util.math.BlockPos

object LiftHelper {

    private val liftShaftMap: LinkedHashMap<Pair<Int, Int>, LinkedHashSet<LiftBlockEntity>> = linkedMapOf()

    fun getOrCreateLiftShaft(pos: BlockPos) = getOrCreateLiftShaft(Pair(pos.x, pos.z))

    private fun getOrCreateLiftShaft(pair: Pair<Int, Int>): LinkedHashSet<LiftBlockEntity> {
        return liftShaftMap.getOrPut(pair) { linkedSetOf() }
    }

}