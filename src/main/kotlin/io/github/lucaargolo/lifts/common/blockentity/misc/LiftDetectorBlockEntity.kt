package io.github.lucaargolo.lifts.common.blockentity.misc

import io.github.lucaargolo.lifts.common.block.misc.LiftDetector
import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import io.github.lucaargolo.lifts.utils.LinkActionResult
import io.github.lucaargolo.lifts.utils.Linkable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper

class LiftDetectorBlockEntity: BlockEntity(BlockEntityCompendium.LIFT_DETECTOR_TYPE), Linkable, Tickable {

    private var linkedPos: BlockPos? = null
    private var linkedLift: LiftBlockEntity? = null

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
        val world = world as? ServerWorld ?: return
        val state = cachedState
        when(state[LiftDetector.STATE]) {
            LiftDetector.State.NOT_LINKED -> {
                linkedLift?.let {
                    world.setBlockState(pos, state.with(LiftDetector.STATE, LiftDetector.State.NOT_HERE))
                }
            }
            else -> {
                if(linkedLift == null || linkedLift?.isRemoved == true) {
                    linkedLift = null
                    world.setBlockState(pos, state.with(LiftDetector.STATE, LiftDetector.State.NOT_LINKED))
                }else if(state[LiftDetector.STATE] == LiftDetector.State.NOT_HERE && linkedLift?.isPlatformHere == true) {
                    world.setBlockState(pos, state.with(LiftDetector.STATE, LiftDetector.State.HERE))
                }else if(state[LiftDetector.STATE] == LiftDetector.State.HERE && linkedLift?.isPlatformHere == false) {
                    world.setBlockState(pos, state.with(LiftDetector.STATE, LiftDetector.State.NOT_HERE))
                }
            }
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