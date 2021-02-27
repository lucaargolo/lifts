package io.github.lucaargolo.lifts.utils

import net.minecraft.util.math.BlockPos

interface Linkable {

    fun link(blockPos: BlockPos): LinkActionResult

}