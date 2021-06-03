package io.github.lucaargolo.lifts.common.blockentity.misc

import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import io.github.lucaargolo.lifts.utils.LinkActionResult
import io.github.lucaargolo.lifts.utils.Linkable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World

class LiftButtonBlockEntity(pos: BlockPos, state: BlockState): BlockEntity(BlockEntityCompendium.LIFT_BUTTON_TYPE, pos, state), Linkable {

    private var linkedPos: BlockPos? = null
    var linkedLift: LiftBlockEntity? = null

    override fun link(blockPos: BlockPos): LinkActionResult{
        return (world?.getBlockEntity(blockPos) as? LiftBlockEntity)?.let {
            val distance = MathHelper.sqrt(blockPos.getSquaredDistance(pos.x+0.0, pos.y+0.0, pos.z+0.0, true).toFloat())
            if(distance > MAX_LIFT_DISTANCE) {
                LinkActionResult.TOO_FAR_AWAY
            }else {
                linkedLift = it
                LinkActionResult.SUCCESSFUL
            }
        } ?: LinkActionResult.NOT_LIFT
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        linkedLift?.let { tag.putLong("linkedLift", it.pos.asLong()) }
        return super.writeNbt(tag)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        linkedPos = if(tag.contains("linkedLift")) {
            BlockPos.fromLong(tag.getLong("linkedLift"))
        } else { null }
    }

    companion object {
        const val MAX_LIFT_DISTANCE = 16

        @Suppress("unused_parameter")
        fun commonTick(world: World, pos: BlockPos, state: BlockState, entity: LiftButtonBlockEntity) {
            entity.linkedPos?.let {
                entity.linkedLift = world.getBlockEntity(entity.linkedPos) as? LiftBlockEntity
                entity.linkedPos = null
            }
        }
    }
}