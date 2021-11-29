package io.github.lucaargolo.lifts.common.blockentity.misc

import io.github.lucaargolo.lifts.common.block.misc.LiftDetector
import io.github.lucaargolo.lifts.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.lifts.common.blockentity.lift.LiftBlockEntity
import io.github.lucaargolo.lifts.utils.LinkActionResult
import io.github.lucaargolo.lifts.utils.Linkable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World

class LiftDetectorBlockEntity(pos: BlockPos, state: BlockState): BlockEntity(BlockEntityCompendium.LIFT_DETECTOR_TYPE, pos, state), Linkable {

    private var linkedPos: BlockPos? = null
    private var linkedLift: LiftBlockEntity? = null

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

    override fun writeNbt(tag: NbtCompound) {
        linkedLift?.let { tag.putLong("linkedLift", it.pos.asLong()) }
        super.writeNbt(tag)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        linkedPos = if(tag.contains("linkedLift")) {
            BlockPos.fromLong(tag.getLong("linkedLift"))
        } else { null }
    }

    companion object {
        const val MAX_LIFT_DISTANCE = 16

        fun commonTick(world: World, pos: BlockPos, state: BlockState, entity: LiftDetectorBlockEntity) {
            entity.linkedPos?.let {
                entity.linkedLift = world.getBlockEntity(entity.linkedPos) as? LiftBlockEntity
                entity.linkedPos = null
            }
            val serverWorld = world as? ServerWorld ?: return
            when(state[LiftDetector.STATE]) {
                LiftDetector.State.NOT_LINKED -> {
                    entity.linkedLift?.let {
                        serverWorld.setBlockState(pos, state.with(LiftDetector.STATE, LiftDetector.State.NOT_HERE))
                    }
                }
                else -> {
                    if(entity.linkedLift == null || entity.linkedLift?.isRemoved == true) {
                        entity.linkedLift = null
                        serverWorld.setBlockState(pos, state.with(LiftDetector.STATE, LiftDetector.State.NOT_LINKED))
                    }else if(state[LiftDetector.STATE] == LiftDetector.State.NOT_HERE && entity.linkedLift?.isPlatformHere == true) {
                        serverWorld.setBlockState(pos, state.with(LiftDetector.STATE, LiftDetector.State.HERE))
                    }else if(state[LiftDetector.STATE] == LiftDetector.State.HERE && entity.linkedLift?.isPlatformHere == false) {
                        serverWorld.setBlockState(pos, state.with(LiftDetector.STATE, LiftDetector.State.NOT_HERE))
                    }
                }
            }
        }

    }


}