package io.github.lucaargolo.lifts.common.blockentity.misc

import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import io.github.lucaargolo.lifts.utils.LinkActionResult
import io.github.lucaargolo.lifts.utils.Linkable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper

class LiftButtonBlockEntity: BlockEntity(BlockEntityCompendium.LIFT_BUTTON_TYPE), Linkable, Tickable {

    private var linkedPos: BlockPos? = null
    var linkedLift: LiftBlockEntity? = null

    override fun link(blockPos: BlockPos): LinkActionResult{
        return (world?.getBlockEntity(blockPos) as? LiftBlockEntity)?.let {
            val distance = MathHelper.sqrt(blockPos.getSquaredDistance(pos.x+0.0, pos.y+0.0, pos.z+0.0, true))
            if(distance > MAX_LIFT_DISTANCE) {
                LinkActionResult.TOO_FAR_AWAY
            }else {
                linkedLift = it
                LinkActionResult.SUCCESSFUL
            }
        } ?: LinkActionResult.NOT_LIFT
    }

    override fun tick() {
        linkedPos?.let {
            linkedLift = world?.getBlockEntity(linkedPos) as? LiftBlockEntity
            linkedPos = null
        }
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        linkedLift?.let { tag.putLong("linkedLift", it.pos.asLong()) }
        return super.toTag(tag)
    }

    override fun fromTag(blockState: BlockState, tag: CompoundTag) {
        super.fromTag(blockState, tag)
        linkedPos = if(tag.contains("linkedLift")) {
            BlockPos.fromLong(tag.getLong("linkedLift"))
        } else { null }
    }

    companion object {
        const val MAX_LIFT_DISTANCE = 16
    }
}